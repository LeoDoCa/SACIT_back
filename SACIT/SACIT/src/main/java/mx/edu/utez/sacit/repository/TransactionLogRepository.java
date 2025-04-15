package mx.edu.utez.sacit.repository;

import mx.edu.utez.sacit.model.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
}
