// Função chamada ao clicar no botão Entrar
function login() {

    // Recupera os valores dos campos
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;

    // Valida se os campos estão preenchidos
    if (!email || !senha) {
        mostrarMensagem('Por favor, preencha todos os campos.', 'danger');
        return;
    }

    // Busca todos os usuários na API
    fetch('/usuarios')
        .then(response => response.json())
        .then(usuarios => {

            // Verifica se existe um usuário com o e-mail e senha informados
            const usuario = usuarios.find(u => u.email === email && u.senha === senha);

            if (usuario) {
                // Salva o usuário logado no localStorage
                localStorage.setItem('usuarioLogado', JSON.stringify(usuario));

                // Redireciona conforme o perfil do usuário
                if (usuario.perfil === 'ADMIN') {
                    window.location.href = 'dashboard-admin.html';
                } else {
                    window.location.href = 'dashboard.html';
                }
            } else {
                mostrarMensagem('E-mail ou senha incorretos.', 'danger');
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