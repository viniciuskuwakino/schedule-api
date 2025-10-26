package com.evoluservices.schedule_api.room;

import com.evoluservices.schedule_api.meeting.dto.ResponseMeetingDto;
import com.evoluservices.schedule_api.room.dto.CreateRoomDto;
import com.evoluservices.schedule_api.room.dto.ResponseRoomDto;
import com.evoluservices.schedule_api.room.dto.UpdateRoomDto;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@Tag(name = "Salas", description = "Endpoints para gerenciamento de salas")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Operation(summary = "Cadastrar uma nova sala")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sala criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da sala inválidos")
    })
    @PostMapping
    public ResponseEntity<ResponseRoomDto> create(@RequestBody CreateRoomDto dto) {
        ResponseRoomDto newRoom = roomService.create(dto);
        return ResponseEntity.ok(newRoom);
    }

    @Operation(summary = "Listar salas com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Salas retornadas com sucesso")
    })
    @GetMapping
    public ResponseEntity<Page<ResponseRoomDto>> findAll(@ParameterObject Pageable pageable) {
        Page<ResponseRoomDto> rooms = roomService.findAll(pageable);
        return ResponseEntity.ok(rooms);
    }

    @Operation(summary = "Buscar detalhes da sala por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sala retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseRoomDto> getById(@PathVariable Long id) {
        ResponseRoomDto room = roomService.findById(id);
        return ResponseEntity.ok(room);
    }

    @Operation(summary = "Listar reuniões agendadas para uma sala")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reuniões retornadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @GetMapping("/{id}/meetings")
    public ResponseEntity<Page<ResponseMeetingDto>> getMeetingsByRoomId(@PathVariable Long id, @ParameterObject Pageable pageable) {
        Page<ResponseMeetingDto> roomMeetings = roomService.findMeetingsByRoomId(id, pageable);
        return ResponseEntity.ok(roomMeetings);
    }


    @Operation(summary = "Atualizar informações da sala")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sala atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseRoomDto> updatePartial(@PathVariable Long id, @RequestBody UpdateRoomDto dto) {
        ResponseRoomDto updatedRoom = roomService.update(id, dto);
        return ResponseEntity.ok(updatedRoom);
    }

    @Operation(summary = "Excluir sala por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sala excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Sala não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
