package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.dto.RequiredDocumentsDto;
import mx.edu.utez.SACIT.model.Procedures;
import mx.edu.utez.SACIT.model.RequiredDocuments;
import mx.edu.utez.SACIT.repository.ProcedureRepository;
import mx.edu.utez.SACIT.repository.RequiredDocumentRepository;
import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RequiredDocumentService {


    private final RequiredDocumentRepository requiredDocumentRepository;


    private final ProcedureRepository procedureRepository;

    public RequiredDocumentService(RequiredDocumentRepository requiredDocumentRepository, ProcedureRepository procedureRepository) {
        this.requiredDocumentRepository = requiredDocumentRepository;
        this.procedureRepository = procedureRepository;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {
        try {
            List<RequiredDocuments> documentsList = requiredDocumentRepository.findAll();
            if (documentsList.isEmpty()) {
       return Utilities.generateResponse(HttpStatus.NOT_FOUND,"No records found","404");

            }
            return Utilities.ResponseWithData(HttpStatus.OK,"Records found successfully","200",documentsList);
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error","500");
        }

    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUuid(UUID uuid) {
        try {
            RequiredDocuments document = requiredDocumentRepository.findByUuid(uuid);
            if (document != null) {
                return Utilities.ResponseWithData(HttpStatus.OK,"Document found","200",document);

            } else {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND,"Document not found","404");

            }
        }catch (IllegalArgumentException e){
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"Invalid UUID","400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error","500");
        }



    }

    @Transactional
    public ResponseEntity<?> save(RequiredDocumentsDto documentDto, UUID procedureUuid) {
        try {
            Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
            if (!optionalProcedure.isPresent()) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"Procedure not found","400");
            }

            RequiredDocuments document = new RequiredDocuments();
            document.setUuid(UUID.randomUUID());
            document.setName(documentDto.getName());
            document.setDescription(documentDto.getDescription());
            document.setMandatory(documentDto.getMandatory());
            document.setProcedure(optionalProcedure.get());

            RequiredDocuments savedDocument = requiredDocumentRepository.save(document);
            return Utilities.ResponseWithData(HttpStatus.CREATED,"Document created successfully","201",savedDocument);

        }catch (IllegalArgumentException e){
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"Invalid UUID","400");
        }

        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> update(UUID uuid, RequiredDocumentsDto documentDto) {
        RequiredDocuments existingDocument = requiredDocumentRepository.findByUuid(uuid);
        if (existingDocument == null) {
            return Utilities.generateResponse(HttpStatus.NOT_FOUND,"Required document not found","404");

        }

        try {
            existingDocument.setName(documentDto.getName());
            existingDocument.setDescription(documentDto.getDescription());
            existingDocument.setMandatory(documentDto.getMandatory());

            RequiredDocuments updatedDocument = requiredDocumentRepository.save(existingDocument);
            return Utilities.ResponseWithData(HttpStatus.OK,"Document updated successfully","200",updatedDocument);
        }catch (IllegalArgumentException e){
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"Invalid Format","400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error","500");
        }
    }

    @Transactional
    public ResponseEntity<?> delete(UUID uuid) {
        RequiredDocuments document = requiredDocumentRepository.findByUuid(uuid);
        if (document == null) {
            return Utilities.generateResponse(HttpStatus.NOT_FOUND,"Required document not found","404");
        }

        try {
            requiredDocumentRepository.delete(document);
            return Utilities.generateResponse(HttpStatus.OK,"Required document deleted successfully","200");

        }catch (IllegalArgumentException e){
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"Invalid UUID","400");
        }
        catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error","500");

        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByProcedure(UUID procedureUuid) {
        Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
        if (!optionalProcedure.isPresent()) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"Procedure not found","400");

        }


        try {
            List<RequiredDocuments> documents = requiredDocumentRepository.findAll().stream()
                    .filter(doc -> doc.getProcedure().getId().equals(optionalProcedure.get().getId()))
                    .toList();

            if (documents.isEmpty()) {
               return Utilities.generateResponse(HttpStatus.NOT_FOUND,"No documents found for this procedure","404");
            }

            return Utilities.ResponseWithData(HttpStatus.OK,"Documents found","200",documents);
        }catch (IllegalArgumentException e){
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"Invalid UUID","400");
        }catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error","500");
        }

    }
}