package br.com.ifpe.oxefood.api.acesso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

import br.com.ifpe.oxefood.modelo.acesso.Usuario;
import br.com.ifpe.oxefood.modelo.acesso.UsuarioService;

@RestController
@RequestMapping("/api/recuperar-senha")
public class RecuperarSenhaController {

    private static final Logger logger = LoggerFactory.getLogger(RecuperarSenhaController.class);

    private final UsuarioService usuarioService;
    private final JavaMailSender mailSender;

    @Autowired
    public RecuperarSenhaController(UsuarioService usuarioService, JavaMailSender mailSender) {
        this.usuarioService = usuarioService;
        this.mailSender = mailSender;
    }

    @PostMapping
    public String recuperarSenha(@RequestBody RecuperarSenhaRequest request) {
        logger.info("Recuperar senha iniciado. Usuário: {}", request.getUsername());

        Usuario usuario = usuarioService.findByUsername(request.getUsername());

        if (usuario == null) {
            logger.warn("Usuário não encontrado: {}", request.getUsername());
            return "Usuário não encontrado!";
        }

        // Gerar um código de verificação
        String codigoVerificacao = gerarCodigoVerificacao();

        // Salvar o código de verificação no usuário
        usuario.setCodigoVerificacao(codigoVerificacao);
        usuarioService.save(usuario);

        // Enviar e-mail com o código de verificação
        enviarEmailVerificacao(usuario.getUsername(), codigoVerificacao);

        return "Código de verificação enviado com sucesso!";
    }

    @PostMapping("/verificar-codigo")
    public String verificarCodigo(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String codigoVerificacao = request.get("codigoVerificacao");

        logger.info("Verificar código iniciado. Usuário: {}, Código: {}", username, codigoVerificacao);

        Usuario usuario = usuarioService.findByUsername(username);

        if (usuario == null) {
            logger.warn("Usuário não encontrado: {}", username);
            return "Usuário não encontrado!";
        }

        // Verificar se o código de verificação está correto
        if (usuario.getCodigoVerificacao() != null && usuario.getCodigoVerificacao().equals(codigoVerificacao)) {
            logger.info("Código de verificação correto. Redefinir senha.");
            // Código de verificação correto, permitir a redefinição de senha
            return "Código de verificação correto. Redefinir senha.";
        } else {
            logger.warn("Código de verificação incorreto. Usuário: {}, Código: {}", username, codigoVerificacao);
            // Código de verificação incorreto
            return "Código de verificação incorreto. Tente novamente.";
        }
    }

    @PostMapping("/redefinir-senha")
    public String redefinirSenha(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String novaSenha = request.get("novaSenha");

        logger.info("Redefinir senha iniciado. Usuário: {}", username);

        Usuario usuario = usuarioService.findByUsername(username);

        if (usuario == null) {
            logger.warn("Usuário não encontrado: {}", username);
            return "Usuário não encontrado!";
        }

        // Atualizar a senha do usuário
        usuario.setPassword(novaSenha);
        usuario.setCodigoVerificacao(null);
        usuarioService.save(usuario);

        return "Senha redefinida com sucesso!";
    }

    private String gerarCodigoVerificacao() {
        // Gerar um código de verificação aleatório
        Random random = new Random();
        int codigo = 1000 + random.nextInt(9000); // Gera um número aleatório entre 1000 e 9999
        return String.valueOf(codigo);
    }

    private void enviarEmailVerificacao(String destinatario, String codigoVerificacao) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinatario);
        message.setSubject("Código de Verificação");
        message.setText("Seu código de verificação é: " + codigoVerificacao);
        mailSender.send(message);
    }
}
