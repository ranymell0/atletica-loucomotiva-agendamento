package br.ufop.agendamento.exception;

// Lançada quando os dados de um agendamento violam alguma regra de negócio
// (ex: data no passado, horário inconsistente, esporte não informado)
public class AgendamentoInvalidoException extends RuntimeException {

    public AgendamentoInvalidoException(String mensagem) {
        super(mensagem);
    }
}