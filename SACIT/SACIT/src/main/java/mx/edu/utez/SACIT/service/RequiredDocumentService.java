package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.dto.RequieredDocumentsDto;
import mx.edu.utez.SACIT.model.Procedures;
import mx.edu.utez.SACIT.model.RequieredDocuments;
import mx.edu.utez.SACIT.repository.ProcedureRepository;
import mx.edu.utez.SACIT.repository.RequieredDocumentRepository;
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
    private RequieredDocumentRepository requieredDocumentRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {
        List<RequieredDocuments> documentsList = requieredDocumentRepository.findAll();
        if (documentsList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(documentsList, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUuid(UUID uuid) {
        RequieredDocuments document = requieredDocumentRepository.findByUuid(uuid);
        if (document != null) {
            return new ResponseEntity<>(document, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<?> save(RequieredDocumentsDto documentDto, UUID procedureUuid) {
        try {
            Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
            if (!optionalProcedure.isPresent()) {
                return new ResponseEntity<>("Procedure not found", HttpStatus.BAD_REQUEST);
            }

            RequieredDocuments document = new RequieredDocuments();
            document.setUuid(UUID.randomUUID());
            document.setName(documentDto.getName());
            document.setDescription(documentDto.getDescription());
            document.setMandatory(documentDto.getMandatory());
            document.setProcedure(optionalProcedure.get());

            RequieredDocuments savedDocument = requieredDocumentRepository.save(document);
            return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> update(UUID uuid, RequieredDocumentsDto documentDto) {
        RequieredDocuments existingDocument = requieredDocumentRepository.findByUuid(uuid);
        if (existingDocument == null) {
            return new ResponseEntity<>("Required document not found", HttpStatus.NOT_FOUND);
        }

        try {
            existingDocument.setName(documentDto.getName());
            existingDocument.setDescription(documentDto.getDescription());
            existingDocument.setMandatory(documentDto.getMandatory());

            RequieredDocuments updatedDocument = requieredDocumentRepository.save(existingDocument);
            return new ResponseEntity<>(updatedDocument, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> delete(UUID uuid) {
        RequieredDocuments document = requieredDocumentRepository.findByUuid(uuid);
        if (document == null) {
            return new ResponseEntity<>("Required document not found", HttpStatus.NOT_FOUND);
        }

        try {
            requieredDocumentRepository.delete(document);
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

        List<RequieredDocuments> documents = requieredDocumentRepository.findAll().stream()
                .filter(doc -> doc.getProcedure().getId().equals(optionalProcedure.get().getId()))
                .toList();

        if (documents.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(documents, HttpStatus.OK);
    }
}