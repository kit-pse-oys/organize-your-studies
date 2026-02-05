package de.pse.oys.controller;

import de.pse.oys.dto.InvalidDtoException;
import de.pse.oys.dto.QuestionnaireDTO;
import de.pse.oys.service.QuestionnaireService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST-Controller für die Bearbeitung des Fragebogens.
 * Ermöglicht es Nutzern, ihre Lernpräferenzen zu übermitteln und den Status
 * ihrer Befragung abzurufen.
 * @author utgid
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/questionnaire")
public class QuestionnaireController extends BaseController {

    private final QuestionnaireService questionnaireService;

    /**
     * Erzeugt eine neue Instanz des QuestionnaireControllers.
     * @param questionnaireService Der Service für die Fragebogen-Logik.
     */
    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    /**
     * Übermittelt die Antworten des Fragebogens für den aktuell angemeldeten Nutzer.
     * @param dto Das DTO mit den Antworten des Fragebogens.
     * @return Eine ResponseEntity, die den Erfolg der Operation signalisiert.
     */
    @PostMapping("/submit")
    public ResponseEntity<Void> submitQuestionnaire(@RequestBody QuestionnaireDTO dto) {
        UUID userId = getAuthenticatedUserId();
        try {
            questionnaireService.submitQuestionnaire(userId, dto);
        } catch (InvalidDtoException | EntityNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Ruft den Status des Fragebogens ab (z. B. ob dieser bereits ausgefüllt wurde).
     * @return Eine ResponseEntity mit den Statusinformationen.
     */
    @GetMapping("/status")
    public ResponseEntity<Boolean> getQuestionnaireStatus() {
        UUID userId = getAuthenticatedUserId();
        try {
            boolean isCompleted = questionnaireService.hasLearningPreferences(userId);
            return ResponseEntity.ok(isCompleted);
        } catch (EntityNotFoundException e) {
            // Nur ein theoretischer Fall, da der Nutzer authentifiziert ist und dementsprechend existieren muss
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
}