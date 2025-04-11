package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.dto.ProceduresDto;
import mx.edu.utez.SACIT.model.Procedures;
import mx.edu.utez.SACIT.model.RequiredDocuments;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.repository.ProcedureRepository;
import mx.edu.utez.SACIT.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProcedureService {

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {
        List<Procedures> proceduresList = procedureRepository.findAll();
        if (proceduresList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(proceduresList, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUuid(UUID uuid) {
        Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(uuid);
        if (optionalProcedure.isPresent()) {
            return new ResponseEntity<>(optionalProcedure.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<?> save(ProceduresDto proceduresDto) {
        try {
            System.out.println("Iniciando guardado de procedimiento: " + proceduresDto.getName());

            Optional<UserModel> optionalUser = userRepository.findById(proceduresDto.getCreatorId());
            if (!optionalUser.isPresent()) {
                System.out.println("Usuario creador no encontrado: " + proceduresDto.getCreatorId());
                return new ResponseEntity<>("Creator not found", HttpStatus.BAD_REQUEST);
            }

            Procedures procedure = new Procedures();
            procedure.setUuid(UUID.randomUUID());
            procedure.setName(proceduresDto.getName());
            procedure.setDescription(proceduresDto.getDescription());
            procedure.setCost(proceduresDto.getCost());
            procedure.setEstimatedTime(proceduresDto.getEstimatedTime());
            procedure.setCreationDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            procedure.setStatus("ACTIVE");
            procedure.setCreator(optionalUser.get());

            if (proceduresDto.getRequiredDocumentsNames() != null
                    && !proceduresDto.getRequiredDocumentsNames().isEmpty()) {
                System.out.println("Procesando documentos requeridos...");
                for (String documentName : proceduresDto.getRequiredDocumentsNames()) {
                    System.out.println("Agregando documento requerido: " + documentName);
                    RequiredDocuments document = new RequiredDocuments();
                    document.setName(documentName);
                    document.setProcedure(procedure);
                    procedure.getRequieredDocuments().add(document);
                }
            }

            Procedures savedProcedure = procedureRepository.save(procedure);
            System.out.println("Procedimiento guardado exitosamente con ID: " + savedProcedure.getId());

            return new ResponseEntity<>(savedProcedure, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error al guardar el procedimiento: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> update(UUID uuid, ProceduresDto proceduresDto) {
        Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(uuid);
        if (!optionalProcedure.isPresent()) {
            return new ResponseEntity<>("Procedure not found", HttpStatus.NOT_FOUND);
        }

        try {
            Procedures procedure = optionalProcedure.get();
            procedure.setName(proceduresDto.getName());
            procedure.setDescription(proceduresDto.getDescription());
            procedure.setCost(proceduresDto.getCost());
            procedure.setEstimatedTime(proceduresDto.getEstimatedTime());
            procedure.setStatus(proceduresDto.getStatus());

            // 👇 Esta parte actualiza los documentos requeridos
            procedure.getRequieredDocuments().clear(); // elimina los anteriores

            if (proceduresDto.getRequiredDocumentsNames() != null
                    && !proceduresDto.getRequiredDocumentsNames().isEmpty()) {
                for (String documentName : proceduresDto.getRequiredDocumentsNames()) {
                    RequiredDocuments doc = new RequiredDocuments();
                    doc.setName(documentName);
                    doc.setProcedure(procedure);
                    procedure.getRequieredDocuments().add(doc);
                }
            }

            Procedures updatedProcedure = procedureRepository.save(procedure);
            return new ResponseEntity<>(updatedProcedure, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> changeStatus(UUID uuid, String status) {
        Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(uuid);
        if (!optionalProcedure.isPresent()) {
            return new ResponseEntity<>("Procedure not found", HttpStatus.NOT_FOUND);
        }

        try {
            Procedures procedure = optionalProcedure.get();
            procedure.setStatus(status);
            Procedures updatedProcedure = procedureRepository.save(procedure);
            return new ResponseEntity<>(updatedProcedure, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> delete(UUID uuid) {
        Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(uuid);
        if (!optionalProcedure.isPresent()) {
            return new ResponseEntity<>("Procedure not found", HttpStatus.NOT_FOUND);
        }

        try {
            procedureRepository.delete(optionalProcedure.get());
            return new ResponseEntity<>("Procedure deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
