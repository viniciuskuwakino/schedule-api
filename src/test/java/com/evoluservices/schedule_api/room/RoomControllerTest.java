package com.evoluservices.schedule_api.room;

import com.evoluservices.schedule_api.meeting.Meeting;
import com.evoluservices.schedule_api.meeting.MeetingRepository;
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
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        meetingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createRoom_returnsCreatedRoom() throws Exception {
        String payload = objectMapper.writeValueAsString(new CreateRoomRequest("Sala A"));

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sala A"));

        assertThat(roomRepository.findAll()).hasSize(1);
    }

    @Test
    void findAll_returnsPaginatedRooms() throws Exception {
        roomRepository.save(new Room(null, "Sala 1", null, null, null));
        roomRepository.save(new Room(null, "Sala 2", null, null, null));

        mockMvc.perform(get("/rooms")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getById_returnsRoom() throws Exception {
        Room saved = roomRepository.save(new Room(null, "Sala B", null, null, null));

        mockMvc.perform(get("/rooms/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Sala B"));
    }

    @Test
    void updatePartial_updatesRoomName() throws Exception {
        Room saved = roomRepository.save(new Room(null, "Sala C", null, null, null));

        String payload = objectMapper.writeValueAsString(new UpdateRoomRequest("Sala C Atualizada"));

        mockMvc.perform(patch("/rooms/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sala C Atualizada"));

        assertThat(roomRepository.findById(saved.getId())).get()
                .extracting(Room::getName)
                .isEqualTo("Sala C Atualizada");
    }

    @Test
    void deleteRoom_removesRoom() throws Exception {
        Room saved = roomRepository.save(new Room(null, "Sala D", null, null, null));

        mockMvc.perform(delete("/rooms/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(roomRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void getMeetingsByRoom_returnsPagedMeetings() throws Exception {
        User user = new User();
        user.setName("João");
        user.setEmail("joao@example.com");
        user.setPassword("senha");
        user = userRepository.save(user);

        Room room = roomRepository.save(new Room(null, "Sala E", null, null, null));

        Meeting meeting = new Meeting();
        meeting.setStartsAt(LocalDateTime.of(2024, 1, 1, 9, 0));
        meeting.setEndsAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        meeting.setUser(user);
        meeting.setRoom(room);
        meetingRepository.save(meeting);

        mockMvc.perform(get("/rooms/" + room.getId() + "/meetings")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].roomId").value(room.getId()))
                .andExpect(jsonPath("$.content[0].userId").value(user.getId()));
    }

    private record CreateRoomRequest(String name) {}

    private record UpdateRoomRequest(String name) {}
}
