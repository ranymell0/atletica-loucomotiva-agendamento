package br.ufop.agendamento.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String perfil;

    // Função do usuário na atlética
    @Column
    private String funcao = "Atleta";

    // Código de administrador informado no cadastro.
    // Não é persistido no banco: existe só durante a requisição,
    // pra o Service decidir o perfil (ADMIN ou ATLETA) do lado do servidor.
    @Transient
    private String codigoAdmin;

    // Modalidades esportivas do usuário
    @ManyToMany
    @JoinTable(
            name = "usuario_esporte",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "esporte_id")
    )
    private List<Esporte> esportes;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getFuncao() { return funcao; }
    public void setFuncao(String funcao) { this.funcao = funcao; }

    public String getCodigoAdmin() { return codigoAdmin; }
    public void setCodigoAdmin(String codigoAdmin) { this.codigoAdmin = codigoAdmin; }

    public List<Esporte> getEsportes() { return esportes; }
    public void setEsportes(List<Esporte> esportes) { this.esportes = esportes; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}