package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.model.TransactionLog;
import mx.edu.utez.SACIT.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionLogService {

    private final TransactionLogRepository repository;

    public TransactionLogService(TransactionLogRepository repository) {
        this.repository = repository;
    }

    public void logTransaction(String transactionType, String tableName, Integer relatedUserId, String details) {
        // Crea una nueva instancia de TransactionLog
        TransactionLog log = new TransactionLog();
        log.setUuid(UUID.randomUUID().toString());  // Cambio aquí, de UUID a String
        log.setTransactionType(transactionType);
        log.setTableName(tableName);
        log.setRelatedUserId(relatedUserId);
        log.setDetails(details);
        log.setTransactionDate(LocalDateTime.now());

        // Guarda el log en la base de datos
        repository.save(log);
    }
}
