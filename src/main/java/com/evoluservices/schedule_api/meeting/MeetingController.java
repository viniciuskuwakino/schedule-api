package com.evoluservices.schedule_api.meeting;

import com.evoluservices.schedule_api.meeting.dto.CreateMeetingDto;
import com.evoluservices.schedule_api.meeting.dto.ResponseMeetingDto;
import com.evoluservices.schedule_api.meeting.dto.UpdateMeetingDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meetings")
@Tag(name = "Reuniões", description = "Operações relacionadas ao agendamento de reuniões")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @Operation(summary = "Criar uma nova reunião")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reunião criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Horário de reunião inválido informado"),
            @ApiResponse(responseCode = "404", description = "Sala ou usuário informado não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito de reunião detectado")
    })
    @PostMapping
    public ResponseEntity<ResponseMeetingDto> create(@RequestBody CreateMeetingDto dto) {
        ResponseMeetingDto newMeeting = meetingService.create(dto);
        return ResponseEntity.ok(newMeeting);
    }

    @Operation(summary = "Listar reuniões com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reuniões retornadas com sucesso")
    })
    @GetMapping
    public ResponseEntity<Page<ResponseMeetingDto>> findAll(@ParameterObject Pageable pageable) {
        Page<ResponseMeetingDto> meetings = meetingService.findAll(pageable);
        return ResponseEntity.ok(meetings);
    }

    @Operation(summary = "Buscar detalhes da reunião por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reunião retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Reunião não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMeetingDto> findById(@PathVariable Long id) {
        ResponseMeetingDto meeting = meetingService.findById(id);
        return ResponseEntity.ok(meeting);
    }

    @Operation(summary = "Atualizar informações da reunião")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reunião atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Horário de reunião inválido informado"),
            @ApiResponse(responseCode = "404", description = "Reunião ou recurso relacionado não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito de reunião detectado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseMeetingDto> update(@PathVariable Long id, @RequestBody UpdateMeetingDto dto) {
        ResponseMeetingDto meeting = meetingService.update(id, dto);
        return ResponseEntity.ok(meeting);
    }

    @Operation(summary = "Excluir reunião por id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reunião excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Reunião não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetingService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
