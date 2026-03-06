package de.pse.oys.freeTime;

import de.pse.oys.domain.FreeTime;
import de.pse.oys.domain.RecurringFreeTime;
import de.pse.oys.domain.SingleFreeTime;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.UserType;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.dto.controller.WrapperDTO;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FreeTimeServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FreeTimeRepository freeTimeRepository;
    private User mockUser;

    private FreeTimeService sut;

    @BeforeEach
    void setUp() {
        sut = new FreeTimeService(userRepository, freeTimeRepository);
        mockUser = new User("TestUser", UserType.LOCAL) {};
        setField(mockUser, "freeTimes", new ArrayList<>());
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
            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of());

            UUID generatedId = UUID.randomUUID();

            when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(invocation -> {
                FreeTime ft = invocation.getArgument(0);
                forceSetId(ft, generatedId);
                return ft;
            });

            UUID result = sut.createFreeTime(userId, input);

            assertThat(result).isEqualTo(generatedId);
            assertThat(mockUser.getFreeTimes()).hasSize(1);
            assertThat(mockUser.getFreeTimes().get(0).getFreeTimeId()).isEqualTo(generatedId);

            verify(freeTimeRepository).save(any(FreeTime.class));
            verify(userRepository).save(mockUser);
        }

        @Test
        void createFreeTime_allowsEarlierTimeSlot_whenExistingStartsAfterNewEnds() {
            UUID userId = UUID.randomUUID();
            LocalDate date = LocalDate.of(2026, 1, 31);

            FreeTimeDTO input = dto("Earlier", date, LocalTime.of(10, 0), LocalTime.of(11, 0), false);

            givenUserExists(userId);

            SingleFreeTime existing = new SingleFreeTime(
                    userId,
                    "Later",
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0),
                    date
            );

            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of(existing));

            UUID generatedId = UUID.randomUUID();
            when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(invocation -> {
                FreeTime ft = invocation.getArgument(0);
                forceSetId(ft, generatedId);
                return ft;
            });

            UUID result = sut.createFreeTime(userId, input);

            assertThat(result).isEqualTo(generatedId);
            verify(freeTimeRepository).save(any(FreeTime.class));
        }

        @Test
        void createsRecurringFreeTime_andReturnsUuid() {
            UUID userId = UUID.randomUUID();
            LocalDate date = LocalDate.of(2026, 2, 2);
            LocalTime start = LocalTime.of(8, 0);
            LocalTime end = LocalTime.of(9, 30);

            FreeTimeDTO input = dto("Training", date, start, end, true);

            givenUserExists(userId);
            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of());

            UUID generatedId = UUID.randomUUID();

            FreeTime savedMock = mock(FreeTime.class);
            when(savedMock.getFreeTimeId()).thenReturn(generatedId);

            when(freeTimeRepository.save(any(FreeTime.class))).thenReturn(savedMock);

            UUID result = sut.createFreeTime(userId, input);

            assertThat(result).isEqualTo(generatedId);
        }

        @Test
        void createFreeTime_throwsResourceNotFound_whenUserMissing() {
            UUID userId = UUID.randomUUID();
            when(userRepository.existsById(userId)).thenReturn(false);

            assertThatThrownBy(() -> sut.createFreeTime(
                    userId,
                    dto("x", LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), false)
            )).isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void createFreeTime_throwsResourceNotFound_whenUserIdNull() {
            assertThatThrownBy(() -> sut.createFreeTime(
                    null,
                    dto("x", LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), false)
            )).isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).getReferenceById(any());
            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void createFreeTime_throwsValidationException_whenDtoInvalid() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = new FreeTimeDTO();

            assertThatThrownBy(() -> sut.createFreeTime(userId, invalid))
                    .isInstanceOf(ValidationException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void createFreeTime_throwsValidationException_whenDtoIsNull() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            assertThatThrownBy(() -> sut.createFreeTime(userId, null))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findAllByUserId(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void createFreeTime_throwsValidationException_whenTitleBlank() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = dto(
                    "   ",
                    LocalDate.of(2026, 1, 1),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    false
            );

            assertThatThrownBy(() -> sut.createFreeTime(userId, invalid))
                    .isInstanceOf(ValidationException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void createFreeTime_throwsValidationException_whenTitleNull() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = dto(
                    null,
                    LocalDate.of(2026, 1, 1),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    false
            );

            assertThatThrownBy(() -> sut.createFreeTime(userId, invalid))
                    .isInstanceOf(ValidationException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void createFreeTime_throwsValidationException_whenDateNull() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = dto(
                    "No Date",
                    null,
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    false
            );

            assertThatThrownBy(() -> sut.createFreeTime(userId, invalid))
                    .isInstanceOf(ValidationException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void createFreeTime_throwsValidationException_whenStartTimeNull() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = dto(
                    "No Start",
                    LocalDate.of(2026, 1, 1),
                    null,
                    LocalTime.of(11, 0),
                    false
            );

            assertThatThrownBy(() -> sut.createFreeTime(userId, invalid))
                    .isInstanceOf(ValidationException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void createFreeTime_throwsValidationException_whenEndTimeNull() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = dto(
                    "No End",
                    LocalDate.of(2026, 1, 1),
                    LocalTime.of(10, 0),
                    null,
                    false
            );

            assertThatThrownBy(() -> sut.createFreeTime(userId, invalid))
                    .isInstanceOf(ValidationException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void createFreeTime_throwsValidationException_whenStartEqualsEnd() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = dto(
                    "Equal",
                    LocalDate.of(2026, 1, 1),
                    LocalTime.of(10, 0),
                    LocalTime.of(10, 0),
                    false
            );

            assertThatThrownBy(() -> sut.createFreeTime(userId, invalid))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findAllByUserId(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void createFreeTime_throwsValidationException_whenStartAfterEnd() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = dto(
                    "Backwards",
                    LocalDate.of(2026, 1, 1),
                    LocalTime.of(12, 0),
                    LocalTime.of(11, 0),
                    false
            );

            assertThatThrownBy(() -> sut.createFreeTime(userId, invalid))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findAllByUserId(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void createFreeTime_throwsValidationException_whenOverlap() {
            UUID userId = UUID.randomUUID();
            FreeTimeDTO input = dto(
                    "Overlap",
                    LocalDate.of(2026, 1, 31),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    false
            );

            givenUserExists(userId);

            FreeTime overlapping = mock(FreeTime.class);
            when(overlapping.occursOn(input.getDate())).thenReturn(true);
            when(overlapping.getStartTime()).thenReturn(LocalTime.of(10, 30));
            when(overlapping.getEndTime()).thenReturn(LocalTime.of(11, 30));

            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of(overlapping));

            assertThatThrownBy(() -> sut.createFreeTime(userId, input))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void createFreeTime_allowsAdjacentTimeSlots_withoutOverlap() {
            UUID userId = UUID.randomUUID();
            LocalDate date = LocalDate.of(2026, 1, 31);

            FreeTimeDTO input = dto("Second", date, LocalTime.of(11, 0), LocalTime.of(12, 0), false);

            givenUserExists(userId);

            FreeTime existing = mock(FreeTime.class);
            when(existing.occursOn(date)).thenReturn(true);
            when(existing.getStartTime()).thenReturn(LocalTime.of(10, 0));
            when(existing.getEndTime()).thenReturn(LocalTime.of(11, 0));

            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of(existing));

            UUID generatedId = UUID.randomUUID();
            when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(invocation -> {
                FreeTime ft = invocation.getArgument(0);
                forceSetId(ft, generatedId);
                return ft;
            });

            UUID result = sut.createFreeTime(userId, input);

            assertThat(result).isEqualTo(generatedId);
            verify(freeTimeRepository).save(any(FreeTime.class));
        }

        @Test
        void createFreeTime_ignoresNullCandidateAndNonOccurringCandidate_inOverlapCheck() {
            UUID userId = UUID.randomUUID();
            LocalDate date = LocalDate.of(2026, 1, 31);

            FreeTimeDTO input = dto("Study", date, LocalTime.of(10, 0), LocalTime.of(11, 0), false);

            givenUserExists(userId);

            FreeTime nonOccurring = mock(FreeTime.class);

            when(freeTimeRepository.findAllByUserId(userId))
                    .thenReturn(java.util.Arrays.asList(null, nonOccurring));

            UUID generatedId = UUID.randomUUID();
            when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(invocation -> {
                FreeTime ft = invocation.getArgument(0);
                forceSetId(ft, generatedId);
                return ft;
            });

            UUID result = sut.createFreeTime(userId, input);

            assertThat(result).isEqualTo(generatedId);
            verify(freeTimeRepository).save(any(FreeTime.class));
        }

        @Test
        void createFreeTime_ignoresExistingFreeTimeOnDifferentDate() {
            UUID userId = UUID.randomUUID();
            LocalDate date = LocalDate.of(2026, 1, 31);

            FreeTimeDTO input = dto("Study", date, LocalTime.of(10, 0), LocalTime.of(11, 0), false);

            givenUserExists(userId);

            SingleFreeTime otherDay = new SingleFreeTime(
                    userId,
                    "Other day",
                    LocalTime.of(10, 30),
                    LocalTime.of(11, 30),
                    LocalDate.of(2026, 2, 1)
            );

            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of(otherDay));

            UUID generatedId = UUID.randomUUID();
            when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(invocation -> {
                FreeTime ft = invocation.getArgument(0);
                forceSetId(ft, generatedId);
                return ft;
            });

            UUID result = sut.createFreeTime(userId, input);

            assertThat(result).isEqualTo(generatedId);
            verify(freeTimeRepository).save(any(FreeTime.class));
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

            FreeTimeDTO input = dto(
                    "New",
                    LocalDate.of(2026, 2, 1),
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0),
                    false
            );

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));
            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of(existing));
            when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(inv -> inv.getArgument(0));

            FreeTimeDTO result = sut.updateFreeTime(userId, freeTimeId, input);

            assertThat(result.getTitle()).isEqualTo("New");
            assertThat(result.isWeekly()).isFalse();
            assertThat(result.getStartTime()).isEqualTo(input.getStartTime());
            assertThat(result.getEndTime()).isEqualTo(input.getEndTime());
            assertThat(result.getDate()).isEqualTo(input.getDate());

            verify(freeTimeRepository).save(existing);
        }

        @Test
        void updatesRecurringFreeTime_successfully_andReturnsWeeklyDto() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            RecurringFreeTime existing = new RecurringFreeTime(
                    userId,
                    "Training",
                    LocalTime.of(8, 0),
                    LocalTime.of(9, 0),
                    DayOfWeek.MONDAY
            );
            forceSetId(existing, freeTimeId);

            FreeTimeDTO input = dto(
                    "Updated Training",
                    LocalDate.of(2026, 2, 9), // ebenfalls Montag
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    true
            );

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));
            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of(existing));
            when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(inv -> inv.getArgument(0));

            FreeTimeDTO result = sut.updateFreeTime(userId, freeTimeId, input);

            assertThat(result.getTitle()).isEqualTo("Updated Training");
            assertThat(result.isWeekly()).isTrue();
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(10, 0));
            assertThat(result.getDate()).isEqualTo(existing.getRepresentativeDate());
        }

        @Test
        void updateFreeTime_skipsSameEntityInOverlapCheck_viaExcludeId() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            SingleFreeTime existing = new SingleFreeTime(
                    userId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            FreeTimeDTO input = dto(
                    "Still Fine",
                    LocalDate.of(2026, 1, 31),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    false
            );

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));
            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of(existing));
            when(freeTimeRepository.save(any(FreeTime.class))).thenAnswer(inv -> inv.getArgument(0));

            FreeTimeDTO result = sut.updateFreeTime(userId, freeTimeId, input);

            assertThat(result.getTitle()).isEqualTo("Still Fine");
            verify(freeTimeRepository).save(existing);
        }

        @Test
        void updateFreeTime_throwsValidationException_whenIdNull() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            assertThatThrownBy(() -> sut.updateFreeTime(
                    userId,
                    null,
                    dto("x", LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), false)
            )).isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findById(any());
        }

        @Test
        void updateFreeTime_throwsValidationException_whenDtoIsNull() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();
            givenUserExists(userId);

            assertThatThrownBy(() -> sut.updateFreeTime(userId, freeTimeId, null))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findById(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void updateFreeTime_throwsResourceNotFound_whenUserMissing() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            when(userRepository.existsById(userId)).thenReturn(false);

            assertThatThrownBy(() -> sut.updateFreeTime(
                    userId,
                    freeTimeId,
                    dto("New", LocalDate.of(2026, 2, 1), LocalTime.of(12, 0), LocalTime.of(13, 0), false)
            )).isInstanceOf(ResourceNotFoundException.class);

            verify(freeTimeRepository, never()).findById(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void updateFreeTime_throwsValidationException_whenStartEqualsEnd() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = dto(
                    "Equal",
                    LocalDate.of(2026, 2, 1),
                    LocalTime.of(10, 0),
                    LocalTime.of(10, 0),
                    false
            );

            assertThatThrownBy(() -> sut.updateFreeTime(userId, freeTimeId, invalid))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findById(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void updateFreeTime_throwsValidationException_whenStartAfterEnd() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();
            givenUserExists(userId);

            FreeTimeDTO invalid = dto(
                    "Backwards",
                    LocalDate.of(2026, 2, 1),
                    LocalTime.of(13, 0),
                    LocalTime.of(12, 0),
                    false
            );

            assertThatThrownBy(() -> sut.updateFreeTime(userId, freeTimeId, invalid))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findById(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void updateFreeTime_throwsResourceNotFound_whenFreeTimeMissing() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();
            givenUserExists(userId);

            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.updateFreeTime(
                    userId,
                    freeTimeId,
                    dto("x", LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), false)
            )).isInstanceOf(ResourceNotFoundException.class);

            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void updateFreeTime_throwsAccessDenied_whenNotOwner() {
            UUID userId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            SingleFreeTime existing = new SingleFreeTime(
                    otherUserId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> sut.updateFreeTime(
                    userId,
                    freeTimeId,
                    dto("New", LocalDate.of(2026, 2, 1), LocalTime.of(12, 0), LocalTime.of(13, 0), false)
            )).isInstanceOf(AccessDeniedException.class);

            verify(freeTimeRepository, never()).findAllByUserId(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void updateFreeTime_throwsValidationException_whenTypeChangeAttempted() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            SingleFreeTime existing = new SingleFreeTime(
                    userId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

            FreeTimeDTO input = dto(
                    "New",
                    LocalDate.of(2026, 2, 2),
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0),
                    true
            );

            assertThatThrownBy(() -> sut.updateFreeTime(userId, freeTimeId, input))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findAllByUserId(any());
            verify(freeTimeRepository, never()).save(any());
        }

        @Test
        void updateFreeTime_throwsValidationException_whenOverlap() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            SingleFreeTime existing = new SingleFreeTime(
                    userId, "Old", LocalTime.of(10, 0), LocalTime.of(11, 0), LocalDate.of(2026, 1, 31)
            );
            forceSetId(existing, freeTimeId);

            FreeTimeDTO input = dto(
                    "New",
                    LocalDate.of(2026, 2, 1),
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0),
                    false
            );

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.of(existing));

            FreeTime overlapping = mock(FreeTime.class);
            when(overlapping.getFreeTimeId()).thenReturn(UUID.randomUUID());
            when(overlapping.occursOn(input.getDate())).thenReturn(true);
            when(overlapping.getStartTime()).thenReturn(LocalTime.of(12, 30));
            when(overlapping.getEndTime()).thenReturn(LocalTime.of(13, 30));

            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of(existing, overlapping));

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
        void deleteFreeTime_throwsValidationException_whenIdNull() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);

            assertThatThrownBy(() -> sut.deleteFreeTime(userId, null))
                    .isInstanceOf(ValidationException.class);

            verify(freeTimeRepository, never()).findById(any());
            verify(freeTimeRepository, never()).delete(any());
        }

        @Test
        void deleteFreeTime_throwsResourceNotFound_whenUserIdNull() {
            UUID freeTimeId = UUID.randomUUID();

            assertThatThrownBy(() -> sut.deleteFreeTime(null, freeTimeId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(freeTimeRepository);
        }

        @Test
        void deleteFreeTime_throwsResourceNotFound_whenMissing() {
            UUID userId = UUID.randomUUID();
            UUID freeTimeId = UUID.randomUUID();

            givenUserExists(userId);
            when(freeTimeRepository.findById(freeTimeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.deleteFreeTime(userId, freeTimeId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(freeTimeRepository, never()).delete(any());
        }

        @Test
        void deleteFreeTime_throwsAccessDenied_whenNotOwner() {
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

    @Nested
    @DisplayName("getFreeTimesByUserId")
    class GetFreeTimesByUserId {

        @Test
        void returnsEmptyList_whenUserHasNoFreeTimes() {
            UUID userId = UUID.randomUUID();
            givenUserExists(userId);
            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of());

            List<WrapperDTO<FreeTimeDTO>> result = sut.getFreeTimesByUserId(userId);

            assertThat(result).isEmpty();
        }

        @Test
        void returnsMappedSingleAndRecurringFreeTimes() {
            UUID userId = UUID.randomUUID();

            SingleFreeTime single = new SingleFreeTime(
                    userId,
                    "Doctor",
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    LocalDate.of(2026, 3, 10)
            );
            UUID singleId = UUID.randomUUID();
            forceSetId(single, singleId);

            RecurringFreeTime recurring = new RecurringFreeTime(
                    userId,
                    "Weekly Training",
                    LocalTime.of(18, 0),
                    LocalTime.of(19, 0),
                    DayOfWeek.TUESDAY
            );
            UUID recurringId = UUID.randomUUID();
            forceSetId(recurring, recurringId);

            givenUserExists(userId);
            when(freeTimeRepository.findAllByUserId(userId)).thenReturn(List.of(single, recurring));

            List<WrapperDTO<FreeTimeDTO>> result = sut.getFreeTimesByUserId(userId);

            assertThat(result).hasSize(2);

            WrapperDTO<FreeTimeDTO> first = result.get(0);
            assertThat(first.getId()).isEqualTo(singleId);
            assertThat(first.getData().getTitle()).isEqualTo("Doctor");
            assertThat(first.getData().isWeekly()).isFalse();
            assertThat(first.getData().getDate()).isEqualTo(LocalDate.of(2026, 3, 10));

            WrapperDTO<FreeTimeDTO> second = result.get(1);
            assertThat(second.getId()).isEqualTo(recurringId);
            assertThat(second.getData().getTitle()).isEqualTo("Weekly Training");
            assertThat(second.getData().isWeekly()).isTrue();
            assertThat(second.getData().getStartTime()).isEqualTo(LocalTime.of(18, 0));
            assertThat(second.getData().getEndTime()).isEqualTo(LocalTime.of(19, 0));
            assertThat(second.getData().getDate()).isEqualTo(recurring.getRepresentativeDate());
        }

        @Test
        void throwsNullPointerException_whenUserIdNull() {
            assertThatThrownBy(() -> sut.getFreeTimesByUserId(null))
                    .isInstanceOf(NullPointerException.class);

            verifyNoInteractions(userRepository, freeTimeRepository);
        }

        @Test
        void throwsResourceNotFound_whenUserDoesNotExist() {
            UUID userId = UUID.randomUUID();
            when(userRepository.existsById(userId)).thenReturn(false);

            assertThatThrownBy(() -> sut.getFreeTimesByUserId(userId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(freeTimeRepository, never()).findAllByUserId(any());
        }
    }

    private void givenUserExists(UUID userId) {
        setField(mockUser, "userId", userId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(mockUser);
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