package com.evoluservices.schedule_api.room;

import com.evoluservices.schedule_api.meeting.dto.ResponseMeetingDto;
import com.evoluservices.schedule_api.meeting.MeetingRepository;
import com.evoluservices.schedule_api.room.dto.CreateRoomDto;
import com.evoluservices.schedule_api.room.dto.ResponseRoomDto;
import com.evoluservices.schedule_api.room.dto.UpdateRoomDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    public ResponseRoomDto create(CreateRoomDto dto) {
        Room newRoom = new Room();
        newRoom.setName(dto.name());

        Room savedRoom = roomRepository.save(newRoom);

        return toResponseDto(savedRoom);

    }

    public Page<ResponseRoomDto> findAll(Pageable pageable) {

        return roomRepository.findAll(pageable)
                .map(this::toResponseDto);

    }

    public ResponseRoomDto findById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada"));

        return toResponseDto(room);
    }

    public Page<ResponseMeetingDto> findMeetingsByRoomId(Long id, Pageable pageable) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada"));

        return meetingRepository.findByRoomId(room.getId(), pageable)
                .map(meeting -> new ResponseMeetingDto(
                        meeting.getId(),
                        meeting.getStartsAt(),
                        meeting.getEndsAt(),
                        meeting.getUser() != null ? meeting.getUser().getId() : null,
                        meeting.getRoom() != null ? meeting.getRoom().getId() : null,
                        meeting.getCreatedAt(),
                        meeting.getUpdatedAt()
                ));

    }

    public ResponseRoomDto update(Long id, UpdateRoomDto dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada"));

        if (dto.name() != null && !dto.name().isBlank()) {
            room.setName(dto.name());
        }

        Room savedRoom = roomRepository.save(room);

        return toResponseDto(savedRoom);
    }

    public void delete(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada"));

        roomRepository.delete(room);
    }

    private ResponseRoomDto toResponseDto(Room room) {
        return new ResponseRoomDto(
                room.getId(),
                room.getName(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }


}
