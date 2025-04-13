package mx.edu.utez.sacit.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {
    private final Map<String, OtpDetails> otpStore = new HashMap<>();

    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(999999 - 100000) + 100000);
        OtpDetails details = new OtpDetails(otp, System.currentTimeMillis() + (5 * 60 * 1000));
        otpStore.put(email, details);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        OtpDetails details = otpStore.get(email);
        if (details == null || details.getExpiry() < System.currentTimeMillis()) {
            otpStore.remove(email);
            return false;
        }
        if (details.getOtp().equals(otp)) {
            otpStore.remove(email);
            return true;
        }
        return false;
    }

    private static class OtpDetails {
        private String otp;
        private long expiry;

        public OtpDetails(String otp, long expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpiry() {
            return expiry;
        }
    }
}

