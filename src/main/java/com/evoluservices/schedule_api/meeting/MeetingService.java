package com.evoluservices.schedule_api.meeting;

import com.evoluservices.schedule_api.meeting.dto.CreateMeetingDto;
import com.evoluservices.schedule_api.meeting.dto.ResponseMeetingDto;
import com.evoluservices.schedule_api.meeting.dto.UpdateMeetingDto;
import com.evoluservices.schedule_api.room.Room;
import com.evoluservices.schedule_api.room.RoomRepository;
import com.evoluservices.schedule_api.user.User;
import com.evoluservices.schedule_api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    public ResponseMeetingDto create(CreateMeetingDto dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Room room = roomRepository.findById(dto.roomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada"));

        LocalDateTime startsAt = dto.startsAt();
        LocalDateTime endsAt = dto.endsAt();

        validateTimeRange(startsAt, endsAt);
        validateRoomAvailability(room.getId(), startsAt, endsAt, null);
        validateUserAvailability(user.getId(), startsAt, endsAt, null);

        Meeting meeting = new Meeting();
        meeting.setStartsAt(startsAt);
        meeting.setEndsAt(endsAt);
        meeting.setUser(user);
        meeting.setRoom(room);

        Meeting savedMeeting = meetingRepository.save(meeting);

        return toResponseDto(savedMeeting);
    }

    public Page<ResponseMeetingDto> findAll(Pageable pageable) {
        return meetingRepository.findAll(pageable)
                .map(this::toResponseDto);
    }

    public ResponseMeetingDto findById(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reunião não encontrada"));

        return toResponseDto(meeting);
    }

    public ResponseMeetingDto update(Long id, UpdateMeetingDto dto) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reunião não encontrada"));

        LocalDateTime startsAt = dto.startsAt() != null ? dto.startsAt() : meeting.getStartsAt();
        LocalDateTime endsAt = dto.endsAt() != null ? dto.endsAt() : meeting.getEndsAt();

        if (dto.userId() != null) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
            meeting.setUser(user);
        }

        if (dto.roomId() != null) {
            Room room = roomRepository.findById(dto.roomId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada"));
            meeting.setRoom(room);
        }

        validateTimeRange(startsAt, endsAt);
        validateRoomAvailability(meeting.getRoom().getId(), startsAt, endsAt, meeting.getId());

        if (meeting.getUser() != null) {
            validateUserAvailability(meeting.getUser().getId(), startsAt, endsAt, meeting.getId());
        }

        meeting.setStartsAt(startsAt);
        meeting.setEndsAt(endsAt);

        Meeting updatedMeeting = meetingRepository.save(meeting);

        return toResponseDto(updatedMeeting);
    }

    public void delete(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reunião não encontrada"));

        meetingRepository.delete(meeting);
    }

    private ResponseMeetingDto toResponseDto(Meeting meeting) {
        Long userId = meeting.getUser() != null ? meeting.getUser().getId() : null;
        Long roomId = meeting.getRoom() != null ? meeting.getRoom().getId() : null;

        return new ResponseMeetingDto(
                meeting.getId(),
                meeting.getStartsAt(),
                meeting.getEndsAt(),
                userId,
                roomId,
                meeting.getCreatedAt(),
                meeting.getUpdatedAt()
        );
    }

    private void validateTimeRange(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (startsAt == null || endsAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datas de início e fim são obrigatórias");
        }

        if (!endsAt.isAfter(startsAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data de término deve ser após a data de início");
        }
    }

    private void validateRoomAvailability(Long roomId, LocalDateTime startsAt, LocalDateTime endsAt, Long meetingId) {
        boolean hasConflict = meetingRepository.existsOverlappingMeeting(roomId, startsAt, endsAt, meetingId);

        if (hasConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma reunião agendada para esta sala neste horário");
        }
    }

    private void validateUserAvailability(Long userId, LocalDateTime startsAt, LocalDateTime endsAt, Long meetingId) {
        boolean hasConflict = meetingRepository.existsOverlappingMeetingForUser(userId, startsAt, endsAt, meetingId);

        if (hasConflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O usuário já possui uma reunião neste horário");
        }
    }

}
