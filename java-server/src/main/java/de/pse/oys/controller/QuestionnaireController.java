package de.pse.oys.controller;

import de.pse.oys.dto.QuestionnaireDTO;
import de.pse.oys.service.QuestionnaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    //TODO: mit RemoteClientTest abgleichen, was übergeben wird. getQuestionnaireStatus prüfen?

    /**
     * Übermittelt die Antworten des Fragebogens für den aktuell angemeldeten Nutzer.
     * @param dto Das DTO mit den Antworten des Fragebogens.
     * @return Eine ResponseEntity, die den Erfolg der Operation signalisiert.
     */
    @PutMapping
    public ResponseEntity<Void> submitQuestionnaire(@RequestBody QuestionnaireDTO dto) {
        UUID userId = getAuthenticatedUserId();
        questionnaireService.submitQuestionnaire(userId, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Ruft den Status des Fragebogens ab (z. B. ob dieser bereits ausgefüllt wurde).
     * @return Eine ResponseEntity mit den Statusinformationen.
     */
    @GetMapping("/status")
    public ResponseEntity<Boolean> getQuestionnaireStatus() {
        UUID userId = getAuthenticatedUserId();
        boolean isCompleted = questionnaireService.hasLearningPreferences(userId);
        return ResponseEntity.ok(isCompleted);


    }
}