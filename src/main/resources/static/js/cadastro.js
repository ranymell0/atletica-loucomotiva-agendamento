// Função chamada ao clicar no botão Cadastrar
function cadastrar() {

    // Recupera os valores dos campos
    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;
    const codigoAdmin = document.getElementById('codigoAdmin').value;

    // Valida se os campos obrigatórios estão preenchidos
    if (!nome || !email || !senha) {
        mostrarMensagem('Por favor, preencha todos os campos.', 'danger');
        return;
    }

    // Define o perfil conforme o código de administrador
    // Código correto -> ADMIN, caso contrário -> ATLETA
    const usuario = {
        nome: nome,
        email: email,
        senha: senha,
        perfil: codigoAdmin === 'LOUCOUFOP' ? 'ADMIN' : 'ATLETA'
    };

    // Envia os dados para a API via POST
    fetch('/usuarios', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(usuario)
    })
        .then(response => {
            if (response.ok) {
                mostrarMensagem('Cadastro realizado com sucesso!', 'success');

                // Redireciona para o login após 1.5 segundos
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 1500);
            } else {
                mostrarMensagem('Erro ao realizar cadastro. Tente novamente.', 'danger');
            }
        })
        .catch(error => {
            mostrarMensagem('Erro ao conectar com o servidor.', 'danger');
        });
}

// Exibe uma mensagem de alerta na tela
function mostrarMensagem(texto, tipo) {
    const div = document.getElementById('mensagem');
    div.innerHTML = `<div class="alert alert-${tipo}">${texto}</div>`;
}