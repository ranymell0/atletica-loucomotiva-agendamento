function login() {
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;

    if (!email || !senha) {
        mostrarMensagem('Por favor, preencha todos os campos.', 'danger');
        return;
    }

    fetch('/usuarios')
        .then(response => response.json())
        .then(usuarios => {
            const usuario = usuarios.find(u => u.email === email && u.senha === senha);

            if (usuario) {
                localStorage.setItem('usuarioLogado', JSON.stringify(usuario));
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

function mostrarMensagem(texto, tipo) {
    const div = document.getElementById('mensagem');
    div.innerHTML = `<div class="alert alert-${tipo}">${texto}</div>`;
}