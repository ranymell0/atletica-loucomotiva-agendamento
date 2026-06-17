package br.ufop.agendamento.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "espacos")
public class Espaco {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(name = "capacidade_maxima")
    private Integer capacidadeMaxima;

    private Boolean ativo = true;

    @ManyToMany
    @JoinTable(
            name = "espaco_esporte",
            joinColumns = @JoinColumn(name = "espaco_id"),
            inverseJoinColumns = @JoinColumn(name = "esporte_id")
    )
    private List<Esporte> esportes;

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getCapacidadeMaxima() { return capacidadeMaxima; }
    public void setCapacidadeMaxima(Integer capacidadeMaxima) { this.capacidadeMaxima = capacidadeMaxima; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public List<Esporte> getEsportes() { return esportes; }
    public void setEsportes(List<Esporte> esportes) { this.esportes = esportes; }
}