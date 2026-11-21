package com.gestaomedicamentos.beckend.repository;

import com.gestaomedicamentos.beckend.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Procura um usuário pelo e-mail (usado no Login com Google e na Recuperação de Perfil)
    Optional<Usuario> findByEmail(String email);

    // Procura um paciente pelo código de 6 dígitos (usado quando o cuidador digita o código para
    // vincular)
    Optional<Usuario> findByCodigoVinculo(String codigoVinculo);
}
