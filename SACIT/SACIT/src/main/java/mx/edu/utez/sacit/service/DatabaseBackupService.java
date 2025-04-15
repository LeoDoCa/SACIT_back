package mx.edu.utez.sacit.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile("dev")
public class DatabaseBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupService.class);

    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${db.url}")
    private String dbUrl;

    private String databaseName;
    private final String BACKUP_DIR = "backups/";
    private final String META_DIR = BACKUP_DIR + ".meta/";

    @PostConstruct
    public void init() {
        String[] parts = dbUrl.split("/");
        databaseName = parts[parts.length - 1].split("\\?")[0];

        new File(BACKUP_DIR).mkdirs();
        new File(META_DIR).mkdirs();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void dailyBackup() {
        backupTables("daily", false, "appointments", "uploaded_documents", "unlogged_users");
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void weeklyBackup() {
        backupTables("weekly", false, "procedures", "required_documents", "users", "windows");
    }

    @Scheduled(cron = "0 0 3 1 * *")
    public void monthlyBackup() {
        backupTables("monthly", false, "roles");
    }

    @Scheduled(cron = "0 0 4 */3 * *")
    public void fullEveryThreeDays() {
        backupTables("every3days", false, "access_log", "transaction_log");
    }

    @Scheduled(cron = "0 0 * * * *")
    public void incrementalHourly() {
        Map<String, String> tables = new HashMap<>();
        tables.put("appointments", "creation_date");
        tables.put("access_log", "date");
        tables.put("transaction_log", "date");

        for (Map.Entry<String, String> entry : tables.entrySet()) {
            incrementalBackup(entry.getKey(), entry.getValue());
        }
    }

    private void backupTables(String type, boolean incremental, String... tables) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("%sbackup_%s_%s.sql", BACKUP_DIR, type, timestamp);

            StringBuilder command = new StringBuilder("mysqldump");
            command.append(" -u").append(dbUsername)
                    .append(" -p").append(dbPassword)
                    .append(" ").append(databaseName);

            for (String table : tables) {
                command.append(" ").append(table);
            }

            command.append(" -r ").append(filename);

            Process process = Runtime.getRuntime().exec(command.toString());
            int processComplete = process.waitFor();

            if (processComplete == 0) {
                logger.info("Respaldo '{}' creado correctamente: {}", type, filename);
            } else {
                logger.error("Error al crear el respaldo '{}'.", type);
            }
        } catch (Exception e) {
            logger.error("Excepción durante respaldo '{}': {}", type, e.getMessage());
        }
    }

    private void incrementalBackup(String table, String dateColumn) {
        try {
            String lastFile = META_DIR + table + ".last";
            String lastDate = readLastDate(lastFile);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("%sincremental_%s_%s.sql", BACKUP_DIR, table, timestamp);

            StringBuilder command = new StringBuilder("mysqldump");
            command.append(" -u").append(dbUsername)
                    .append(" -p").append(dbPassword)
                    .append(" ").append(databaseName)
                    .append(" ").append(table);

            if (lastDate != null) {
                command.append(" --where=\"").append(dateColumn).append(" > '").append(lastDate).append("'\"");
            }

            command.append(" -r ").append(filename);

            Process process = Runtime.getRuntime().exec(command.toString());
            int result = process.waitFor();

            if (result == 0) {
                logger.info("Respaldo incremental para '{}' generado: {}", table, filename);
                writeCurrentDate(lastFile);
            } else {
                logger.error("Error en respaldo incremental de '{}'.", table);
            }

        } catch (Exception e) {
            logger.error("Fallo respaldo incremental '{}': {}", table, e.getMessage());
        }
    }

    private String readLastDate(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private void writeCurrentDate(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        } catch (IOException e) {
            logger.warn("No se pudo escribir la fecha en '{}': {}", filePath, e.getMessage());
        }
    }
}