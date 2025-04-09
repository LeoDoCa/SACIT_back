package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.dto.RequiredDocumentsDto;
import mx.edu.utez.SACIT.model.Procedures;
import mx.edu.utez.SACIT.model.RequiredDocuments;
import mx.edu.utez.SACIT.repository.ProcedureRepository;
import mx.edu.utez.SACIT.repository.RequiredDocumentRepository;
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

    @Autowired
    private RequiredDocumentRepository requiredDocumentRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {
        List<RequiredDocuments> documentsList = requiredDocumentRepository.findAll();
        if (documentsList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(documentsList, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUuid(UUID uuid) {
        RequiredDocuments document = requiredDocumentRepository.findByUuid(uuid);
        if (document != null) {
            return new ResponseEntity<>(document, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<?> save(RequiredDocumentsDto documentDto, UUID procedureUuid) {
        try {
            Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
            if (!optionalProcedure.isPresent()) {
                return new ResponseEntity<>("Procedure not found", HttpStatus.BAD_REQUEST);
            }

            RequiredDocuments document = new RequiredDocuments();
            document.setUuid(UUID.randomUUID());
            document.setName(documentDto.getName());
            document.setDescription(documentDto.getDescription());
            document.setMandatory(documentDto.getMandatory());
            document.setProcedure(optionalProcedure.get());

            RequiredDocuments savedDocument = requiredDocumentRepository.save(document);
            return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> update(UUID uuid, RequiredDocumentsDto documentDto) {
        RequiredDocuments existingDocument = requiredDocumentRepository.findByUuid(uuid);
        if (existingDocument == null) {
            return new ResponseEntity<>("Required document not found", HttpStatus.NOT_FOUND);
        }

        try {
            existingDocument.setName(documentDto.getName());
            existingDocument.setDescription(documentDto.getDescription());
            existingDocument.setMandatory(documentDto.getMandatory());

            RequiredDocuments updatedDocument = requiredDocumentRepository.save(existingDocument);
            return new ResponseEntity<>(updatedDocument, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> delete(UUID uuid) {
        RequiredDocuments document = requiredDocumentRepository.findByUuid(uuid);
        if (document == null) {
            return new ResponseEntity<>("Required document not found", HttpStatus.NOT_FOUND);
        }

        try {
            requiredDocumentRepository.delete(document);
            return new ResponseEntity<>("Required document deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByProcedure(UUID procedureUuid) {
        Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
        if (!optionalProcedure.isPresent()) {
            return new ResponseEntity<>("Procedure not found", HttpStatus.BAD_REQUEST);
        }

        List<RequiredDocuments> documents = requiredDocumentRepository.findAll().stream()
                .filter(doc -> doc.getProcedure().getId().equals(optionalProcedure.get().getId()))
                .toList();

        if (documents.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(documents, HttpStatus.OK);
    }
}