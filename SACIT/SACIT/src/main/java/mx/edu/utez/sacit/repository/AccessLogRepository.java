package mx.edu.utez.sacit.repository;

import mx.edu.utez.sacit.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}