package br.com.ifpe.oxefood.api.acesso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecuperarSenhaRequest {
    private String username;
}
