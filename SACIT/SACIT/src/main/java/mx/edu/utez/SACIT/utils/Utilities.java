package mx.edu.utez.SACIT.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utilities {
    private Utilities(){throw new UnsupportedOperationException("Utility class");}

    public static ResponseEntity<Object> generateResponse(HttpStatus status,String mssage, String actionCode){
        Map<String,Object> map = new HashMap<>();
        try{
            map.put("date", new Date());
            map.put("status", status.value());
            map.put("message", mssage);
            map.put("actionCode", actionCode);
            return  new ResponseEntity<>(map,status);
        }catch (Exception e){
            map.clear();
            map.put("date", new Date());
            map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            map.put("message",e.getMessage());
            return new ResponseEntity<>(map,status);
        }
    }
}
