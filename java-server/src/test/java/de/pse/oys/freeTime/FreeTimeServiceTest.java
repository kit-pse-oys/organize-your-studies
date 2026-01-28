package de.pse.oys.freeTime;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.RecurringFreeTime;
import de.pse.oys.domain.SingleFreeTime;
import de.pse.oys.domain.User;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.persistence.FreeTimeRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.FreeTimeService;
import de.pse.oys.dto.InvalidDtoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für FreeTimeService.
 *
 * @author uqvfm
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class FreeTimeServiceTest {

    @Mock UserRepository userRepository;
    @Mock FreeTimeRepository freeTimeRepository;

    private FreeTimeService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new FreeTimeService(userRepository, freeTimeRepository);
        userId = UUID.randomUUID();
    }

    // ------------------------------------------------------------
    // createFreeTime(...)
    // ------------------------------------------------------------

    @Test
    void createFreeTime_userMissing_throwsIllegalArgument() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFreeTime(userId, validOnceDto()))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(freeTimeRepository);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createFreeTime_missingRequiredFields_throwsInvalidDtoException() {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        FreeTimeDTO dto = new FreeTimeDTO(
                null,                       // title fehlt
                LocalDate.of(2026, 1, 28),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                false
        );

        assertThatThrownBy(() -> service.createFreeTime(userId, dto))
                .isInstanceOf(InvalidDtoException.class);

        verify(userRepository, never()).save(any());
        verify(user, never()).addFreeTime(any());
        verifyNoInteractions(freeTimeRepository);
    }

    @Test
    void createFreeTime_invalidRange_throwsInvalidDtoException() {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        FreeTimeDTO dto = new FreeTimeDTO(
                "Bad",
                LocalDate.of(2026, 1, 28),
                LocalTime.of(20, 0),
                LocalTime.of(18, 0), // start >= end
                false
        );

        assertThatThrownBy(() -> service.createFreeTime(userId, dto))
                .isInstanceOf(InvalidDtoException.class);

        verify(userRepository, never()).save(any());
        verify(user, never()).addFreeTime(any());
        verifyNoInteractions(freeTimeRepository);
    }

    @Test
    void createFreeTime_overlapsExistingOnce_throwsInvalidDtoException() {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        LocalDate date = LocalDate.of(2026, 1, 28);

        // echte Entity: Service castet in occursSameDay()
        FreeTime existing = new SingleFreeTime(
                "Existing",
                LocalTime.of(19, 0),
                LocalTime.of(21, 0),
                date
        );

        when(user.getFreeTimes()).thenReturn(List.of(existing));

        FreeTimeDTO dto = new FreeTimeDTO(
                "New",
                date,
                LocalTime.of(20, 0), // überschneidet sich
                LocalTime.of(22, 0),
                false
        );

        assertThatThrownBy(() -> service.createFreeTime(userId, dto))
                .isInstanceOf(InvalidDtoException.class);

        verify(userRepository, never()).save(any());
        verify(user, never()).addFreeTime(any());
        verifyNoInteractions(freeTimeRepository);
    }

    @Test
    void createFreeTime_boundaryAdjacentIntervals_allowed_startEndHalfOpen() {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        LocalDate date = LocalDate.of(2026, 1, 28);

        FreeTime existing = new SingleFreeTime(
                "Existing",
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                date
        );

        when(user.getFreeTimes()).thenReturn(List.of(existing));

        // [18:00,20:00) und [20:00,22:00) => darf NICHT überlappen
        FreeTimeDTO dto = new FreeTimeDTO(
                "Adjacent",
                date,
                LocalTime.of(20, 0),
                LocalTime.of(22, 0),
                false
        );

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        FreeTimeDTO result = service.createFreeTime(userId, dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Adjacent");
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(result.isWeekly()).isFalse();

        verify(user).addFreeTime(any(FreeTime.class));
        verify(userRepository).save(user);
        verifyNoInteractions(freeTimeRepository);
    }

    @Test
    void createFreeTime_weeklyValid_createsRecurringFreeTime_andReturnsWeeklyDto() {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getFreeTimes()).thenReturn(new ArrayList<>());

        LocalDate monday = LocalDate.of(2026, 2, 2); // Montag
        FreeTimeDTO dto = new FreeTimeDTO(
                "Gym",
                monday,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                true
        );

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        FreeTimeDTO result = service.createFreeTime(userId, dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Gym");
        assertThat(result.isWeekly()).isTrue();
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(10, 0));
        // mapToDto berechnet das nächste Datum für den Wochentag -> DayOfWeek muss passen
        assertThat(result.getDate().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        verify(user).addFreeTime(any(FreeTime.class));
        verify(userRepository).save(user);
        verifyNoInteractions(freeTimeRepository);
    }

    @Test
    void createFreeTime_overlapsExistingWeeklySameDay_throwsInvalidDtoException() {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // bestehende wöchentliche Freizeit am Montag 10-12
        FreeTime existingWeekly = new RecurringFreeTime(
                "Weekly",
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                DayOfWeek.MONDAY
        );

        when(user.getFreeTimes()).thenReturn(List.of(existingWeekly));

        // DTO an einem Montag 11-13 => Überlappung
        LocalDate monday = LocalDate.of(2026, 2, 2);
        FreeTimeDTO dto = new FreeTimeDTO(
                "New",
                monday,
                LocalTime.of(11, 0),
                LocalTime.of(13, 0),
                false
        );

        assertThatThrownBy(() -> service.createFreeTime(userId, dto))
                .isInstanceOf(InvalidDtoException.class);

        verify(userRepository, never()).save(any());
        verify(user, never()).addFreeTime(any());
        verifyNoInteractions(freeTimeRepository);
    }

    // ------------------------------------------------------------
    // updateFreeTime(...)
    // ------------------------------------------------------------

    @Test
    void updateFreeTime_freeTimeNotFound_throwsIllegalArgument() {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UUID freeTimeId = UUID.randomUUID();
        when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateFreeTime(userId, freeTimeId, validOnceDto()))
                .isInstanceOf(IllegalArgumentException.class);

        verify(freeTimeRepository, never()).save(any());
    }

    @Test
    void updateFreeTime_notOwned_throwsIllegalArgument() throws Exception {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getFreeTimes()).thenReturn(List.of()); // besitzt es nicht

        UUID freeTimeId = UUID.randomUUID();

        SingleFreeTime existing = new SingleFreeTime(
                "Existing",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalDate.of(2026, 1, 28)
        );
        setFreeTimeId(existing, freeTimeId);

        when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateFreeTime(userId, freeTimeId, validOnceDto()))
                .isInstanceOf(IllegalArgumentException.class);

        verify(freeTimeRepository, never()).save(any());
        verify(freeTimeRepository, never()).delete(any());
    }

    @Test
    void updateFreeTime_invalidRange_throwsInvalidDtoException() throws Exception {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UUID freeTimeId = UUID.randomUUID();

        SingleFreeTime existing = new SingleFreeTime(
                "Existing",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalDate.of(2026, 1, 28)
        );
        setFreeTimeId(existing, freeTimeId);

        when(user.getFreeTimes()).thenReturn(List.of(existing));
        when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

        FreeTimeDTO dto = new FreeTimeDTO(
                "BadRange",
                LocalDate.of(2026, 1, 28),
                LocalTime.of(12, 0),
                LocalTime.of(11, 0), // invalid
                false
        );

        assertThatThrownBy(() -> service.updateFreeTime(userId, freeTimeId, dto))
                .isInstanceOf(InvalidDtoException.class);

        verify(freeTimeRepository, never()).save(any());
        verify(freeTimeRepository, never()).delete(any());
    }

    @Test
    void updateFreeTime_overlaps_throwsInvalidDtoException() throws Exception {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        LocalDate date = LocalDate.of(2026, 1, 28);

        UUID freeTimeId = UUID.randomUUID();

        SingleFreeTime existing = new SingleFreeTime(
                "Existing",
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                date
        );
        setFreeTimeId(existing, freeTimeId);

        // zweiter Block im User, der Overlap erzeugt
        FreeTime other = new SingleFreeTime(
                "Other",
                LocalTime.of(19, 0),
                LocalTime.of(21, 0),
                date
        );
        // "other" muss eine andere ID haben, ignoreId soll nur existing ignorieren
        setFreeTimeId(other, UUID.randomUUID());

        when(user.getFreeTimes()).thenReturn(List.of(existing, other));
        when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

        FreeTimeDTO dto = new FreeTimeDTO(
                "Update",
                date,
                LocalTime.of(20, 0), // overlap mit Other (19-21)
                LocalTime.of(22, 0),
                false
        );

        assertThatThrownBy(() -> service.updateFreeTime(userId, freeTimeId, dto))
                .isInstanceOf(InvalidDtoException.class);

        verify(freeTimeRepository, never()).save(any());
        verify(freeTimeRepository, never()).delete(any());
    }

    @Test
    void updateFreeTime_sameType_updatesAndSaves() throws Exception {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UUID freeTimeId = UUID.randomUUID();

        SingleFreeTime existing = new SingleFreeTime(
                "Existing",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalDate.of(2026, 1, 28)
        );
        setFreeTimeId(existing, freeTimeId);

        when(user.getFreeTimes()).thenReturn(List.of(existing));
        when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));
        when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(inv -> inv.getArgument(0));

        FreeTimeDTO dto = new FreeTimeDTO(
                "Updated",
                LocalDate.of(2026, 1, 28),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                false
        );

        FreeTimeDTO result = service.updateFreeTime(userId, freeTimeId, dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated");
        assertThat(result.isWeekly()).isFalse();
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 1, 28));

        verify(freeTimeRepository).save(existing);
        verify(freeTimeRepository, never()).delete(any());
    }

    @Test
    void updateFreeTime_typeSwitch_deletesOld_savesReplacement() throws Exception {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UUID freeTimeId = UUID.randomUUID();

        SingleFreeTime existing = new SingleFreeTime(
                "Existing",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalDate.of(2026, 1, 28)
        );
        setFreeTimeId(existing, freeTimeId);

        when(user.getFreeTimes()).thenReturn(List.of(existing));
        when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));
        when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(inv -> inv.getArgument(0));

        // Wechsel auf weekly (Recurring)
        LocalDate monday = LocalDate.of(2026, 2, 2); // Montag
        FreeTimeDTO dto = new FreeTimeDTO(
                "WeeklyNew",
                monday,
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                true
        );

        FreeTimeDTO result = service.updateFreeTime(userId, freeTimeId, dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("WeeklyNew");
        assertThat(result.isWeekly()).isTrue();
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(19, 0));
        assertThat(result.getDate().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        verify(user).deleteFreeTime(existing);
        verify(freeTimeRepository).delete(existing);
        verify(user).addFreeTime(any(FreeTime.class));
        verify(freeTimeRepository).save(any(FreeTime.class));
    }

    // ------------------------------------------------------------
    // deleteFreeTime(...)
    // ------------------------------------------------------------

    @Test
    void deleteFreeTime_valid_deletesFromRepository() throws Exception {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UUID freeTimeId = UUID.randomUUID();

        SingleFreeTime existing = new SingleFreeTime(
                "Existing",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalDate.of(2026, 1, 28)
        );
        setFreeTimeId(existing, freeTimeId);

        when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));
        when(user.getFreeTimes()).thenReturn(List.of(existing));

        service.deleteFreeTime(userId, freeTimeId);

        verify(user).deleteFreeTime(existing);
        verify(freeTimeRepository).delete(existing);
    }

    @Test
    void deleteFreeTime_notOwned_throwsIllegalArgument() throws Exception {
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UUID freeTimeId = UUID.randomUUID();

        SingleFreeTime existing = new SingleFreeTime(
                "Existing",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalDate.of(2026, 1, 28)
        );
        setFreeTimeId(existing, freeTimeId);

        when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));
        when(user.getFreeTimes()).thenReturn(List.of()); // gehört nicht dem User

        assertThatThrownBy(() -> service.deleteFreeTime(userId, freeTimeId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(freeTimeRepository, never()).delete(any());
        verify(user, never()).deleteFreeTime(any());
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private FreeTimeDTO validOnceDto() {
        return new FreeTimeDTO(
                "Test",
                LocalDate.of(2026, 1, 28),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                false
        );
    }

    /**
     * Setzt die @GeneratedValue-ID in Tests per Reflection, damit Ownership/IgnoreId funktionieren.
     */
    private static void setFreeTimeId(FreeTime freeTime, UUID id) throws Exception {
        Field f = FreeTime.class.getDeclaredField("freeTimeId");
        f.setAccessible(true);
        f.set(freeTime, id);
    }
}
