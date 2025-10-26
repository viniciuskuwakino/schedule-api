package com.evoluservices.schedule_api.meeting;

import com.evoluservices.schedule_api.meeting.dto.CreateMeetingDto;
import com.evoluservices.schedule_api.meeting.dto.UpdateMeetingDto;
import com.evoluservices.schedule_api.room.Room;
import com.evoluservices.schedule_api.room.RoomRepository;
import com.evoluservices.schedule_api.user.User;
import com.evoluservices.schedule_api.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User defaultUser;
    private Room defaultRoom;

    @BeforeEach
    void setUp() {
        meetingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        defaultUser = new User();
        defaultUser.setName("Maria Teste");
        defaultUser.setEmail("maria@example.com");
        defaultUser.setPassword("senha");
        defaultUser = userRepository.save(defaultUser);

        defaultRoom = new Room();
        defaultRoom.setName("Sala Principal");
        defaultRoom = roomRepository.save(defaultRoom);
    }

    @Test
    void createMeeting_returnsCreatedMeeting() throws Exception {
        CreateMeetingDto dto = new CreateMeetingDto(
                LocalDateTime.of(2024, 1, 1, 9, 0),
                LocalDateTime.of(2024, 1, 1, 10, 0),
                defaultUser.getId(),
                defaultRoom.getId()
        );

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(defaultUser.getId()))
                .andExpect(jsonPath("$.roomId").value(defaultRoom.getId()));

        assertThat(meetingRepository.count()).isEqualTo(1);
    }

    @Test
    void createMeeting_missingDates_returnsBadRequest() throws Exception {
        CreateMeetingDto dto = new CreateMeetingDto(null, null, defaultUser.getId(), defaultRoom.getId());

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMeeting_endBeforeStart_returnsBadRequest() throws Exception {
        CreateMeetingDto dto = new CreateMeetingDto(
                LocalDateTime.of(2024, 1, 1, 11, 0),
                LocalDateTime.of(2024, 1, 1, 10, 0),
                defaultUser.getId(),
                defaultRoom.getId()
        );

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMeeting_conflictingRoom_returnsConflict() throws Exception {
        // existing meeting in same room (9–10)
        Meeting meeting = new Meeting();
        meeting.setRoom(defaultRoom);
        meeting.setUser(defaultUser);
        meeting.setStartsAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        meeting.setEndsAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        meetingRepository.save(meeting);

        CreateMeetingDto dto = new CreateMeetingDto(
                LocalDateTime.of(2024, 1, 1, 9, 30),
                LocalDateTime.of(2024, 1, 1, 10, 30),
                defaultUser.getId(),
                defaultRoom.getId()
        );

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createMeeting_conflictingUser_returnsConflict() throws Exception {
        Room anotherRoom = new Room();
        anotherRoom.setName("Sala Dois");
        anotherRoom = roomRepository.save(anotherRoom);

        Meeting meeting = new Meeting();
        meeting.setRoom(anotherRoom);
        meeting.setUser(defaultUser);
        meeting.setStartsAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        meeting.setEndsAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        meetingRepository.save(meeting);

        CreateMeetingDto dto = new CreateMeetingDto(
                LocalDateTime.of(2024, 1, 1, 9, 30),
                LocalDateTime.of(2024, 1, 1, 10, 30),
                defaultUser.getId(),
                defaultRoom.getId()
        );

        mockMvc.perform(post("/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void findAll_returnsPageOfMeetings() throws Exception {
        meetingRepository.save(buildMeeting(LocalDateTime.of(2024, 1, 1, 9, 0), LocalDateTime.of(2024, 1, 1, 10, 0)));
        meetingRepository.save(buildMeeting(LocalDateTime.of(2024, 1, 2, 9, 0), LocalDateTime.of(2024, 1, 2, 10, 0)));

        mockMvc.perform(get("/meetings")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void findById_returnsMeeting() throws Exception {
        Meeting meeting = meetingRepository.save(buildMeeting(
                LocalDateTime.of(2024, 1, 3, 9, 0),
                LocalDateTime.of(2024, 1, 3, 10, 0)));

        mockMvc.perform(get("/meetings/" + meeting.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(meeting.getId()))
                .andExpect(jsonPath("$.userId").value(defaultUser.getId()));
    }

    @Test
    void updateMeeting_changesTimesSuccessfully() throws Exception {
        Meeting meeting = meetingRepository.save(buildMeeting(
                LocalDateTime.of(2024, 1, 4, 9, 0),
                LocalDateTime.of(2024, 1, 4, 10, 0)));

        UpdateMeetingDto dto = new UpdateMeetingDto(
                LocalDateTime.of(2024, 1, 4, 11, 0),
                LocalDateTime.of(2024, 1, 4, 12, 0),
                null,
                null
        );

        mockMvc.perform(patch("/meetings/" + meeting.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startsAt").value("2024-01-04T11:00:00"))
                .andExpect(jsonPath("$.endsAt").value("2024-01-04T12:00:00"));
    }

    @Test
    void updateMeeting_conflictWithRoom_returnsConflict() throws Exception {
        Meeting existing = meetingRepository.save(buildMeeting(
                LocalDateTime.of(2024, 1, 5, 9, 0),
                LocalDateTime.of(2024, 1, 5, 10, 0)));

        Meeting toUpdate = meetingRepository.save(buildMeeting(
                LocalDateTime.of(2024, 1, 6, 9, 0),
                LocalDateTime.of(2024, 1, 6, 10, 0)));

        UpdateMeetingDto dto = new UpdateMeetingDto(
                LocalDateTime.of(2024, 1, 5, 9, 30),
                LocalDateTime.of(2024, 1, 5, 10, 30),
                null,
                null
        );

        mockMvc.perform(patch("/meetings/" + toUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteMeeting_removesEntry() throws Exception {
        Meeting meeting = meetingRepository.save(buildMeeting(
                LocalDateTime.of(2024, 1, 7, 9, 0),
                LocalDateTime.of(2024, 1, 7, 10, 0)));

        mockMvc.perform(delete("/meetings/" + meeting.getId()))
                .andExpect(status().isNoContent());

        assertThat(meetingRepository.existsById(meeting.getId())).isFalse();
    }

    private Meeting buildMeeting(LocalDateTime startsAt, LocalDateTime endsAt) {
        Meeting meeting = new Meeting();
        meeting.setRoom(defaultRoom);
        meeting.setUser(defaultUser);
        meeting.setStartsAt(startsAt);
        meeting.setEndsAt(endsAt);
        return meeting;
    }
}
