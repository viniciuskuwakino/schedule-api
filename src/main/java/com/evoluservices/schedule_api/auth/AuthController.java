package com.evoluservices.schedule_api.auth;

import com.evoluservices.schedule_api.user.dto.CreateUserDto;
import com.evoluservices.schedule_api.auth.dto.LoginDto;
import com.evoluservices.schedule_api.auth.dto.LoginResponseDto;
import com.evoluservices.schedule_api.user.User;
import com.evoluservices.schedule_api.user.UserRepository;
import com.evoluservices.schedule_api.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para autenticação e cadastro de usuários")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Operation(summary = "Autenticar credenciais do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas informadas")
    })
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDto dto) {
        User user = userRepository.findByEmail(dto.email()).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (passwordEncoder.matches(dto.password(), user.getPassword())) {
            String token = tokenService.generateToken(user);
            return ResponseEntity.ok(new LoginResponseDto(user.getName(), token));
        }

        return ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Cadastrar um novo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity register(@RequestBody CreateUserDto dto) {
        Optional<User> user = userRepository.findByEmail(dto.email());

        if (user.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Usuário já existe com este e-mail"
            );
        }

        User newUser = new User();

        newUser.setName(dto.name());
        newUser.setEmail(dto.email());
        newUser.setPassword(passwordEncoder.encode(dto.password()));

        userRepository.save(newUser);

        String token = tokenService.generateToken(newUser);
        return ResponseEntity.ok(new LoginResponseDto(newUser.getName(), token));
    }
}
