package org.fmazmz.messagemanager.adapter.web.room;

import jakarta.validation.Valid;
import org.fmazmz.messagemanager.adapter.web.ApiResponse;
import org.fmazmz.messagemanager.application.room.RoomApplicationService;
import org.fmazmz.messagemanager.application.room.dto.CreateRoomRequest;
import org.fmazmz.messagemanager.application.room.dto.RoomResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        path = "api/v1/rooms",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
)
public class RoomController {

    private final RoomApplicationService roomApplicationService;

    public RoomController(RoomApplicationService roomApplicationService) {
        this.roomApplicationService = roomApplicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@RequestBody @Valid CreateRoomRequest request) {
        RoomResponse created = roomApplicationService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> listRooms() {
        return ResponseEntity.ok(ApiResponse.success(roomApplicationService.listRooms()));
    }

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(roomApplicationService.getRoom(id)));
    }
}
