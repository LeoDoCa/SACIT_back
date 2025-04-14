package mx.edu.utez.sacit.service;

import mx.edu.utez.sacit.model.TransactionLog;
import mx.edu.utez.sacit.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionLogService {

    private final TransactionLogRepository repository;

    public TransactionLogService(TransactionLogRepository repository) {
        this.repository = repository;
    }

    public void logTransaction(String transactionType, String tableName, String relatedUuid, String details) {
        TransactionLog log = new TransactionLog();
        log.setUuid(UUID.randomUUID().toString());
        log.setTransactionType(transactionType);
        log.setTableName(tableName);
        log.setRelatedUuid(relatedUuid);
        log.setDetails(details);
        log.setTransactionDate(LocalDateTime.now());

        repository.save(log);
    }
}
