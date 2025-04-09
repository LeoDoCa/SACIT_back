package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}