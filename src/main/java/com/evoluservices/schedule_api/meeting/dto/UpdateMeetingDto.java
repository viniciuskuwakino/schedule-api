package com.evoluservices.schedule_api.meeting.dto;

import java.time.LocalDateTime;

public record UpdateMeetingDto(
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Long userId,
        Long roomId
) {
}
