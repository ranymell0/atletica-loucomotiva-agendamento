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

    // Envia e-mail e senha para o backend autenticar.
    // O backend é quem verifica se as credenciais batem, sem expor
    // a lista completa de usuários pro navegador.
    fetch('/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email: email, senha: senha })
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            // Credenciais inválidas
            throw new Error('Credenciais inválidas');
        })
        .then(usuario => {
            // Salva o usuário logado no localStorage
            localStorage.setItem('usuarioLogado', JSON.stringify(usuario));
            // Redireciona conforme o perfil do usuário
            if (usuario.perfil === 'ADMIN') {
                window.location.href = 'dashboard-admin.html';
            } else {
                window.location.href = 'dashboard.html';
            }
        })
        .catch(error => {
            mostrarMensagem('E-mail ou senha incorretos.', 'danger');
        });
}

// Exibe uma mensagem de alerta na tela
function mostrarMensagem(texto, tipo) {
    const div = document.getElementById('mensagem');
    div.innerHTML = `<div class="alert alert-${tipo}">${texto}</div>`;
}