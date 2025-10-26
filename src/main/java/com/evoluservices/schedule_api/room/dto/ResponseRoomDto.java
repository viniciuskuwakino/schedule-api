package com.evoluservices.schedule_api.room.dto;

import java.time.LocalDateTime;

public record ResponseRoomDto(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
