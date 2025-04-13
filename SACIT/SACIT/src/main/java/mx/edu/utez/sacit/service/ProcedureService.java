package mx.edu.utez.sacit.service;

import mx.edu.utez.sacit.dto.ProceduresDto;
import mx.edu.utez.sacit.model.Procedures;
import mx.edu.utez.sacit.model.RequiredDocuments;
import mx.edu.utez.sacit.model.UserModel;
import mx.edu.utez.sacit.repository.ProcedureRepository;
import mx.edu.utez.sacit.repository.UserRepository;
import mx.edu.utez.sacit.utils.Utilities;
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


    private  final ProcedureRepository procedureRepository;

    private  final UserRepository userRepository;

    public ProcedureService(ProcedureRepository procedureRepository, UserRepository userRepository) {
        this.procedureRepository = procedureRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {

        try{
            List<Procedures> proceduresList = procedureRepository.findAll();
            if (proceduresList.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "No records found", "404");
            }
            return Utilities.ResponseWithData(HttpStatus.OK, "Records found successfully", "200", proceduresList);
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }

    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUuid(UUID uuid) {
        try {
            Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(uuid);
            if (optionalProcedure.isPresent()) {
return Utilities.ResponseWithData(HttpStatus.OK, "Record found successfully", "200", optionalProcedure.get());
            }else {
             return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Procedure not found", "404");
            }
        }catch (IllegalArgumentException e){
return Utilities.generateResponse(HttpStatus.NOT_FOUND,"Invalid UUID","400");
        }catch (Exception e){
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error","500");
        }

    }

    @Transactional
    public ResponseEntity<?> save(ProceduresDto proceduresDto) {
        try {

            Optional<UserModel> optionalUser = userRepository.findById(proceduresDto.getCreatorId());
            if (!optionalUser.isPresent()) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Creator not found", "400");

            }

            Procedures procedure = new Procedures();
            procedure.setUuid(UUID.randomUUID());
            procedure.setName(proceduresDto.getName());
            procedure.setDescription(proceduresDto.getDescription());
            procedure.setCost(proceduresDto.getCost());
            procedure.setStimatedTime(proceduresDto.getEstimatedTime());
            procedure.setCreationDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            procedure.setStatus("ACTIVE");
            procedure.setCreator(optionalUser.get());

            if (proceduresDto.getRequiredDocumentsNames() != null
                    && !proceduresDto.getRequiredDocumentsNames().isEmpty()) {
                for (String documentName : proceduresDto.getRequiredDocumentsNames()) {
                    RequiredDocuments document = new RequiredDocuments();
                    document.setName(documentName);
                    document.setProcedure(procedure);
                    procedure.getRequieredDocuments().add(document);
                }
            }

            Procedures savedProcedure = procedureRepository.save(procedure);
return Utilities.ResponseWithData(HttpStatus.CREATED, "Procedure created successfully", "201", savedProcedure);
        }catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID", "400");
        } catch (NullPointerException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Null value found", "400");
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
            procedure.setStimatedTime(proceduresDto.getEstimatedTime());
            procedure.setStatus(proceduresDto.getStatus());

            procedure.getRequieredDocuments().clear();

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
return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Procedure not found", "404");
        }

        try {
            Procedures procedure = optionalProcedure.get();
            procedure.setStatus(status);
            Procedures updatedProcedure = procedureRepository.save(procedure);
            return Utilities.ResponseWithData(HttpStatus.OK, "Procedure status updated successfully", "200", updatedProcedure);

        }catch (IllegalArgumentException e) {
return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID", "400");
        } catch (NullPointerException e) {
return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Null value found", "400");
        }
        catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");

        }
    }

    @Transactional
    public ResponseEntity<?> delete(UUID uuid) {
        try {
            Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(uuid);
            if (!optionalProcedure.isPresent()) {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Procedure not found", "404");
            }
            procedureRepository.delete(optionalProcedure.get());
            return Utilities.generateResponse(HttpStatus.OK, "Procedure deleted successfully", "200");

        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID", "400");
        }catch (Exception e) {

            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }


}
