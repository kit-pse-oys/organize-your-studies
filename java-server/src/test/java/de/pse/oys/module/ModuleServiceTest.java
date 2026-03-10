package de.pse.oys.module;

import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.dto.ModuleDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.ModuleService;
import de.pse.oys.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModuleServiceTest {

    private UserRepository userRepository;
    private ModuleRepository moduleRepository;
    private ModuleService moduleService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        moduleRepository = mock(ModuleRepository.class);
        moduleService = new ModuleService(userRepository, moduleRepository);
    }

    private Module createModuleSpy(String title, ModulePriority priority) {
        Module module = spy(new Module(title, priority));
        UUID id = UUID.randomUUID();
        when(module.getModuleId()).thenReturn(id);
        return module;
    }

    @Test
    void updateModule_success() {
        // GIVEN
        User user = new LocalUser("test", "pass");
        Module existing = createModuleSpy("Old", ModulePriority.LOW);
        user.addModule(existing);

        ModuleDTO dto = new ModuleDTO();
        dto.setId(existing.getModuleId());
        dto.setTitle("New");
        dto.setPriority(ModulePriority.MEDIUM);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN
        moduleService.updateModule(userId, dto);

        // THEN
        verify(existing).setTitle("New");
        verify(moduleRepository).save(existing);
    }

    @Test
    void createModule_success() {
        User user = new LocalUser("test", "pass");
        ModuleDTO dto = new ModuleDTO();
        dto.setTitle("Analysis");
        dto.setPriority(ModulePriority.HIGH);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(moduleRepository.save(any(Module.class))).thenAnswer(i -> {
            Module m = i.getArgument(0);
            Module spyModule = spy(m);
            when(spyModule.getModuleId()).thenReturn(UUID.randomUUID());
            return spyModule;
        });

        UUID generatedId = moduleService.createModule(userId, dto);

        assertNotNull(generatedId);
        verify(moduleRepository).save(any(Module.class));
    }

    @Test
    void deleteModule_success() {
        User user = new LocalUser("test", "pass");
        Module module = createModuleSpy("Delete Me", ModulePriority.LOW);
        user.addModule(module);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Wir löschen mit der ID, die zurückgegeben wird
        moduleService.deleteModule(userId, module.getModuleId());

        verify(moduleRepository).delete(module);
        assertTrue(user.getModules().isEmpty());
    }

    @Test
    void createModule_failsWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> moduleService.createModule(userId, new ModuleDTO()));
    }

    @Test
    void updateModule_failsWhenIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> moduleService.updateModule(userId, new ModuleDTO()));
    }

    @Test
    void updateModule_failsWhenNotOwned() {
        User user = new LocalUser("test", "pass");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ModuleDTO dto = new ModuleDTO();
        dto.setId(UUID.randomUUID());
        dto.setTitle("Title");

        assertThrows(SecurityException.class, () -> moduleService.updateModule(userId, dto));
    }

    @Test
    void deleteModule_failsWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> moduleService.deleteModule(userId, UUID.randomUUID()));
    }

    @Test
    void getModulesByUserId_success() {
        Module m = new Module("M1", ModulePriority.HIGH);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(moduleRepository.findAllByUser_UserId(userId)).thenReturn(List.of(m));

        List<WrapperDTO<ModuleDTO>> result = moduleService.getModulesByUserId(userId);

        assertEquals(1, result.size());
        assertEquals("M1", result.get(0).getData().getTitle());
    }

    @Test
    void getModulesByUserId_failsWhenUserNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> moduleService.getModulesByUserId(userId));
    }

    @Test
    void validateData_allBranches() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(new LocalUser("u", "p")));

        // DTO null
        assertThrows(IllegalArgumentException.class, () -> moduleService.createModule(userId, null));

        // Title null
        ModuleDTO dtoNull = new ModuleDTO();
        assertThrows(IllegalArgumentException.class, () -> moduleService.createModule(userId, dtoNull));

        // Title blank
        ModuleDTO dtoBlank = new ModuleDTO();
        dtoBlank.setTitle("   ");
        assertThrows(IllegalArgumentException.class, () -> moduleService.createModule(userId, dtoBlank));
    }
}