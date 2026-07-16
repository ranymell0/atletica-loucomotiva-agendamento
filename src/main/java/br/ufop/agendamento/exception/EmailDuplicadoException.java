package br.ufop.agendamento.exception;

// Lançada quando se tenta cadastrar um usuário com um e-mail que já existe no sistema
public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException(String email) {
        super("Já existe um usuário cadastrado com o e-mail: " + email);
    }
}