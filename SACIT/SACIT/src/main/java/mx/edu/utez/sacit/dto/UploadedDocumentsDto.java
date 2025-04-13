package mx.edu.utez.sacit.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UploadedDocumentsDto {
    private UUID requiredDocumentUuid;
    private byte[] document;
}