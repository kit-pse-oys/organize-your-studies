package de.pse.oys;

import de.pse.oys.controller.GlobalExceptionHandler;
import de.pse.oys.dto.InvalidDtoException;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testklasse für den globalen Exception-Handler.
 * Deckt alle Fehlerbehandlungsmethoden ab, um die Coverage zu maximieren.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Standalone-Setup mit dem ExceptionHandler und einem Test-Controller
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testHandleNotFound_ResourceNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found-resource"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Ressource fehlt"));
    }

    @Test
    void testHandleNotFound_EntityNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found-entity"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Entität fehlt"));
    }

    @Test
    void testHandleForbidden_AccessDenied() throws Exception {
        mockMvc.perform(get("/test/forbidden-access"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Zugriff verweigert: Keine Rechte")));
    }

    @Test
    void testHandleBadRequest_Validation() throws Exception {
        mockMvc.perform(get("/test/bad-request-validation"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Validierungsfehler"));
    }

    @Test
    void testHandleBadRequest_InvalidDto() throws Exception {
        mockMvc.perform(get("/test/bad-request-dto"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("DTO ungültig"));
    }

    @Test
    void testHandleGeneralError() throws Exception {
        mockMvc.perform(get("/test/general-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Ein unerwarteter interner Fehler ist aufgetreten:")));
    }

    /**
     * Ein interner Hilfs-Controller, der nur Exceptions wirft.
     */
    @RestController
    private static class ExceptionTestController {

        @GetMapping("/test/not-found-resource")
        public void throwResourceNotFound() {
            throw new ResourceNotFoundException("Ressource fehlt");
        }

        @GetMapping("/test/not-found-entity")
        public void throwEntityNotFound() {
            throw new EntityNotFoundException("Entität fehlt");
        }

        @GetMapping("/test/forbidden-access")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Keine Rechte");
        }

        @GetMapping("/test/bad-request-validation")
        public void throwValidation() {
            throw new ValidationException("Validierungsfehler");
        }

        @GetMapping("/test/bad-request-dto")
        public void throwInvalidDto() {
            throw new InvalidDtoException("DTO ungültig");
        }

        @GetMapping("/test/general-error")
        public void throwGeneral() throws Exception {
            throw new Exception("Schwerer Fehler");
        }
    }
}