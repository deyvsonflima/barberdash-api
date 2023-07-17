package br.com.ifpe.oxefood.api.acesso;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ifpe.oxefood.modelo.acesso.Usuario;
import br.com.ifpe.oxefood.modelo.acesso.UsuarioService;
import br.com.ifpe.oxefood.security.jwt.JwtTokenProvider;
import br.com.ifpe.oxefood.util.entity.GenericController;

@RestController
@RequestMapping("/api/login")
public class AuthenticationController extends GenericController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticationController(UsuarioService usuarioService, AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public Map<Object, Object> signin(@RequestBody AuthenticationRequest data) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(data.getUsername(), data.getPassword()));

            Usuario usuario = usuarioService.findByUsername(data.getUsername());
            String token = jwtTokenProvider.createToken(usuario.getUsername(), usuario.getRoles());
            String refreshToken = jwtTokenProvider.createRefreshToken(usuario.getUsername());

            Map<Object, Object> model = new HashMap<>();
            model.put("username", usuario.getUsername());
            model.put("token", token);
            model.put("refresh", refreshToken);

            return model;

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username/password supplied");
        }
    }
}
