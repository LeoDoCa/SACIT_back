package mx.edu.utez.sacit.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Profile("dev")
public class DatabaseBackupService {

    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${db.url}")
    private String dbUrl;

    private String databaseName;

    private final String BACKUP_DIR = "backups/";

    @PostConstruct
    public void init() {
        String[] parts = dbUrl.split("/");
        databaseName = parts[parts.length - 1].split("\\?")[0];

        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void dailyBackup() {
        backupTables("daily", "uploaded_documents", "procedures","appointments");
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void weeklyBackup() {
        backupTables("weekly", "window_schedule", "available_schedules", "roles", "users", "password_reset_token");
    }

    @Scheduled(cron = "0 0 3 1 * *")
    public void monthlyBackup() {
        backupTables("monthly", "windows", "required_documents", "procedures", "roles_users");
    }

    private void backupTables(String type, String... tables) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("%sbackup_%s_%s.sql", BACKUP_DIR, type, timestamp);

            StringBuilder command = new StringBuilder("mysqldump");
            command.append(" -u").append(dbUsername);
            command.append(" -p").append(dbPassword);
            command.append(" ").append(databaseName);
            for (String table : tables) {
                command.append(" ").append(table);
            }
            command.append(" -r ").append(filename);

            Process process = Runtime.getRuntime().exec(command.toString());
            int processComplete = process.waitFor();

            if (processComplete == 0) {
                System.out.println("Respaldo " + type + " creado correctamente: " + filename);
            } else {
                System.err.println("Error al crear el respaldo " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
