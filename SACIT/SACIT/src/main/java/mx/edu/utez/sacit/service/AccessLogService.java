package mx.edu.utez.sacit.service;

import mx.edu.utez.sacit.model.AccessLog;
import mx.edu.utez.sacit.repository.AccessLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccessLogService {

    private final AccessLogRepository logRepository;

    public AccessLogService(AccessLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void registerEvent(String user, String action, String ip, String resource) {
        AccessLog aL = new AccessLog();
        aL.setUser(user);
        aL.setAction(action);
        aL.setIp(ip);
        aL.setDate(LocalDateTime.now());
        aL.setResource(resource);
        logRepository.save(aL);
    }
}
