package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
}
