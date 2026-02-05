package de.pse.oys.freeTime;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.RecurringFreeTime;
import de.pse.oys.domain.SingleFreeTime;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.persistence.FreeTimeRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.FreeTimeService;
import de.pse.oys.service.exception.AccessDeniedException;
import de.pse.oys.service.exception.ResourceNotFoundException;
import de.pse.oys.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FreeTimeServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FreeTimeRepository freeTimeRepository;

    private FreeTimeService sut;

    @BeforeEach
    void setUp() {
        sut = new FreeTimeService(userRepository, freeTimeRepository);
    }

    @Nested
    @DisplayName("createFreeTime")
    class CreateFreeTime {

        @Test
        void createsSingleFreeTime_andReturnsUuid() {
            UUID userId = UUID.randomUUID();
            LocalDate date = LocalDate.of(2026, 1, 31);
            LocalTime start = LocalTime.of(10, 0);
            LocalTime end = LocalTime.of(11, 0);

            FreeTimeDTO input = dto("Gym", date, start, end, false);

            givenUserExists(userId);
            givenNoOverlap(userId, input, null);

            UUID generatedId = UUID.randomUUID();

            FreeTime savedMock = mock(FreeTime.class);
            when(savedMock.getFreeTimeId()).thenReturn(generatedId);

            when(freeTimeRepository.save(any(FreeTime.class))).thenReturn(savedMock);

            UUID result = sut.createFreeTime(userId, input);

            assertThat(result).isEqualTo(generatedId);

            ArgumentCaptor<FreeTime> saved = ArgumentCaptor.forClass(FreeTime.class);
            verify(freeTimeRepository).save(saved.capture());

            assertThat(saved.getValue()).isInstanceOf(SingleFreeTime.class);
            assertThat(saved.getValue().getTitle()).isEqualTo("Gym");
            assertThat(saved.getValue().getStartTime()).isEqualTo(start);
            assertThat(saved.getValue().getEndTime()).isEqualTo(end);
            assertThat(((SingleFreeTime) saved.getValue()).getDate()).isEqualTo(date);
        }


        @Test
        void createsRecurringFreeTime_andReturnsUuid_withoutReflection() {
            UUID userId = UUID.randomUUID();
            LocalDate date = LocalDate.of(2026, 2, 2);
            LocalTime start = LocalTime.of(8, 0);
            LocalTime end = LocalTime.of(9, 30);

            FreeTimeDTO input = dto("Training", date, start, end, true);

            givenUserExists(userId);
            givenNoOverlap(userId, input, null);

            UUID generatedId = UUID.randomUUID();

            FreeTime savedMock = mock(FreeTime.class);
            when(savedMock.getFreeTimeId()).thenReturn(generatedId);

            when(freeTimeRepository.save(any(FreeTime.class))).thenReturn(savedMock);

            UUID result = sut.createFreeTime(userId, input);

            assertThat(result).isEqualTo(generatedId);

            ArgumentCaptor<FreeTime> captor = ArgumentCaptor.forClass(FreeTime.class);
            verify(freeTimeRepository).save(captor.capture());

            assertThat(captor.getValue()).isInstanceOf(RecurringFreeTime.class);
            assertThat(captor.getValue().getTitle()).isEqualTo("Training");
            assertThat(captor.getValue().getStartTime()).isEqualTo(start);
            assertThat(captor.getValue().getEndTime()).isEqualTo(end);
        }


        @Test
        void throwsResourceNotFound_whenUserMissing() {
            UUID userId = UUID.randomUUID();
            when(userRepository.existsById(userId)).thenReturn(false);

            assertThatThrownBy(() -> sut.createFreeTime(userId, dto("x",
                    LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), false)))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void throwsValidationException_whenDtoInvalid() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = new FreeTimeDTO(); // title/date/start/end fehlen

            assertThatThrownBy(() -> sut.createFreeTime(userId, invalid))
                    .isInstanceOf(ValidationException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void throwsValidationException_whenOverlap() {
            UUID userId = UUID.randomUUID();
            FreeTimeDTO input = dto("Overlap",
                    LocalDate.of(2026, 1, 31),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    false);

            givenUserExists(userId);
            givenOverlap(userId, input, null);

            assertThatThrownBy(() -> sut.createFreeTime(userId, input))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateFreeTime")
    class UpdateFreeTime {

        @Test
        void updatesSingleFreeTime_successfully() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            SingleFreeTime existing = new SingleFreeTime(
                    userId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            FreeTimeDTO input = dto("New",
                    LocalDate.of(2026, 2, 1),
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0),
                    false);

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));
            givenNoOverlap(userId, input, freeTimeId);

            when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(inv -> inv.getArgument(0));

            FreeTimeDTO result = sut.updateFreeTime(userId, freeTimeId, input);

            assertThat(result.getTitle()).isEqualTo("New");
            assertThat(result.isWeekly()).isFalse();
            assertThat(result.getStartTime()).isEqualTo(input.getStartTime());
            assertThat(result.getEndTime()).isEqualTo(input.getEndTime());
            assertThat(result.getDate()).isEqualTo(input.getDate());

            // Der Service aktualisiert "existing" und speichert genau dieses Objekt
            verify(freeTimeRepository).save(existing);
        }

        @Test
        void throwsValidationException_whenIdNull() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            assertThatThrownBy(() -> sut.updateFreeTime(userId, null, dto("x",
                    LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), false)))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findById(any());
        }

        @Test
        void throwsResourceNotFound_whenFreeTimeMissing() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();
            givenUserExists(userId);

            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.updateFreeTime(userId, freeTimeId, dto("x",
                    LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), false)))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void throwsAccessDenied_whenNotOwner() {
            UUID userId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            SingleFreeTime existing = new SingleFreeTime(
                    otherUserId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> sut.updateFreeTime(userId, freeTimeId, dto("New",
                    LocalDate.of(2026, 2, 1), LocalTime.of(12, 0), LocalTime.of(13, 0), false)))
                    .isInstanceOf(AccessDeniedException.class);

            verify(freeTimeRepository, never()).findAllByUserId(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void throwsValidationException_whenTypeChangeAttempted() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            // existing: Single
            SingleFreeTime existing = new SingleFreeTime(
                    userId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

            // input: weekly=true => Typwechsel
            FreeTimeDTO input = dto("New",
                    LocalDate.of(2026, 2, 2), LocalTime.of(12, 0), LocalTime.of(13, 0), true);

            assertThatThrownBy(() -> sut.updateFreeTime(userId, freeTimeId, input))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findAllByUserId(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void throwsValidationException_whenOverlap() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            SingleFreeTime existing = new SingleFreeTime(
                    userId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            FreeTimeDTO input = dto("New",
                    LocalDate.of(2026, 2, 1), LocalTime.of(12, 0), LocalTime.of(13, 0), false);

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));
            givenOverlap(userId, input, freeTimeId);

            assertThatThrownBy(() -> sut.updateFreeTime(userId, freeTimeId, input))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteFreeTime")
    class DeleteFreeTime {

        @Test
        void deletesSuccessfully_whenOwnerMatches() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            SingleFreeTime existing = new SingleFreeTime(
                    userId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

            sut.deleteFreeTime(userId, freeTimeId);

            verify(freeTimeRepository).delete(existing);
        }

        @Test
        void throwsResourceNotFound_whenMissing() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.deleteFreeTime(userId, freeTimeId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(freeTimeRepository, never()).delete(any());
        }

        @Test
        void throwsAccessDenied_whenNotOwner() {
            UUID userId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            SingleFreeTime existing = new SingleFreeTime(
                    otherUserId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> sut.deleteFreeTime(userId, freeTimeId))
                    .isInstanceOf(AccessDeniedException.class);

            verify(freeTimeRepository, never()).delete(any());
        }
    }

    // -------------------------
    // Fixtures / stubbing helpers
    // -------------------------

    private void givenUserExists(UUID userId) {
        when(userRepository.existsById(userId)).thenReturn(true);
    }

    private void givenNoOverlap(UUID userId, FreeTimeDTO dto, UUID ignoreId) {
        when(freeTimeRepository.findAllByUserId(eq(userId)))
                .thenReturn(List.of());
    }

    private void givenOverlap(UUID userId, FreeTimeDTO dto, UUID ignoreId) {
        // Ein Eintrag, der am dto.date gilt und zeitlich überlappt.
        FreeTime overlapping = mock(FreeTime.class);

        when(overlapping.getFreeTimeId()).thenReturn(UUID.randomUUID());
        when(overlapping.occursOn(eq(dto.getDate()))).thenReturn(true);
        when(overlapping.getStartTime()).thenReturn(dto.getStartTime());
        when(overlapping.getEndTime()).thenReturn(dto.getEndTime());

        when(freeTimeRepository.findAllByUserId(eq(userId)))
                .thenReturn(List.of(overlapping));
    }

    private static FreeTimeDTO dto(String title, LocalDate date, LocalTime start, LocalTime end, boolean weekly) {
        FreeTimeDTO dto = new FreeTimeDTO();
        dto.setTitle(title);
        dto.setDate(date);
        dto.setStartTime(start);
        dto.setEndTime(end);
        dto.setWeekly(weekly);
        return dto;
    }

    /**
     * Simuliert "Entity aus DB geladen": JPA hätte die ID gesetzt.
     */
    private static void forceSetId(FreeTime entity, UUID id) {
        setField(entity, "freeTimeId", id);
    }

    private static void setField(Object target, String fieldName, Object value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field f = type.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("Field '" + fieldName + "' not found in class hierarchy.");
    }
}
