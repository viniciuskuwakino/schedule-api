package com.evoluservices.schedule_api.meeting;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM Meeting m
            WHERE m.room.id = :roomId
              AND (:meetingId is null or m.id <> :meetingId)
              AND m.startsAt < :endsAt
              AND m.endsAt > :startsAt
            """)
    boolean existsOverlappingMeeting(
            @Param("roomId") Long roomId,
            @Param("startsAt") LocalDateTime startsAt,
            @Param("endsAt") LocalDateTime endsAt,
            @Param("meetingId") Long meetingId
    );

    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM Meeting m
            WHERE m.user.id = :userId
              AND (:meetingId is null or m.id <> :meetingId)
              AND m.startsAt < :endsAt
              AND m.endsAt > :startsAt
            """)
    boolean existsOverlappingMeetingForUser(
            @Param("userId") Long userId,
            @Param("startsAt") LocalDateTime startsAt,
            @Param("endsAt") LocalDateTime endsAt,
            @Param("meetingId") Long meetingId
    );

    Page<Meeting> findByRoomId(Long roomId, Pageable pageable);
}
