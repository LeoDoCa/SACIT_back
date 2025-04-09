package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.model.AccessLog;
import mx.edu.utez.SACIT.repository.AccessLogRepository;
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
