package mx.edu.utez.sacit.utils;

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
public static ResponseEntity<Object>ResponseWithData(HttpStatus status, String mssage,String actionCode, Object data) {
    Map<String, Object> map = new HashMap<>();
    try {
        map.put("date", new Date());
        map.put("status", status.value());
        map.put("message", mssage);
        map.put("actionCode", actionCode);
        map.put("data", data);
        return new ResponseEntity<>(map, status);
    } catch (Exception e) {
        map.clear();
        map.put("date", new Date());
        map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        map.put("message", e.getMessage());
        return new ResponseEntity<>(map, status);
    }
}
}
