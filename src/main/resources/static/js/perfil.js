// Recupera o usuário logado do localStorage
const usuario = JSON.parse(localStorage.getItem('usuarioLogado'));

// Redireciona para o login se não houver usuário logado
if (!usuario) {
    window.location.href = 'login.html';
}

// Exibe o nome do usuário na navbar
document.getElementById('nomeUsuario').textContent = usuario.nome;

// Ajusta a navegação conforme o perfil de quem está logado.
// Admin volta para o painel administrativo e não vê os botões de atleta.
if (usuario.perfil === 'ADMIN') {
    document.getElementById('linkLogo').setAttribute('href', 'dashboard-admin.html');
    document.getElementById('linkVoltar').setAttribute('href', 'dashboard-admin.html');
    document.getElementById('btnNovoAgendamento').style.display = 'none';
    document.getElementById('btnHistorico').style.display = 'none';
}

// Remove o usuário do localStorage ao sair
function logout() {
    localStorage.removeItem('usuarioLogado');
}

// Preenche os dados do perfil no card lateral
document.getElementById('avatarInicial').textContent = usuario.nome.charAt(0).toUpperCase();
document.getElementById('perfilNome').textContent = usuario.nome;
document.getElementById('perfilEmail').textContent = usuario.email;
document.getElementById('perfilFuncao').textContent = usuario.funcao || 'Atleta';

// Define o badge conforme o perfil do usuário
const badge = document.getElementById('perfilBadge');
if (usuario.perfil === 'ADMIN') {
    badge.textContent = 'ADMIN';
    badge.classList.add('bg-danger');
} else {
    badge.textContent = 'ATLETA';
    badge.classList.add('bg-secondary');
}

// Exibe as modalidades do usuário no card lateral
function exibirEsportesPerfil(esportes) {
    const div = document.getElementById('perfilEsportes');
    if (!esportes || esportes.length === 0) {
        div.innerHTML = '<p class="text-muted small">Nenhuma modalidade selecionada</p>';
        return;
    }
    div.innerHTML = esportes.map(e =>
        `<span class="badge bg-dark me-1 mb-1">${e.nome}</span>`
    ).join('');
}

// Preenche os campos do formulário com os dados atuais
document.getElementById('nome').value = usuario.nome;
document.getElementById('email').value = usuario.email;
document.getElementById('funcao').value = usuario.funcao || 'Atleta';

// Carrega os esportes disponíveis e cria os checkboxes
function carregarEsportes() {
    fetch('/esportes')
        .then(response => response.json())
        .then(esportes => {
            const div = document.getElementById('checkboxEsportes');

            // IDs dos esportes que o usuário já pratica
            const esportesUsuario = usuario.esportes ? usuario.esportes.map(e => e.id) : [];

            // Exibe as modalidades do usuário no card lateral
            exibirEsportesPerfil(usuario.esportes);

            // Cria um checkbox para cada esporte disponível
            div.innerHTML = esportes.map(e => `
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="checkbox"
                           id="esporte_${e.id}" value="${e.id}"
                           ${esportesUsuario.includes(e.id) ? 'checked' : ''}>
                    <label class="form-check-label" for="esporte_${e.id}">${e.nome}</label>
                </div>
            `).join('');
        })
        .catch(error => {
            console.error('Erro ao carregar esportes:', error);
        });
}

// Carrega os esportes ao abrir a página
carregarEsportes();

// Salva as alterações do perfil na API
function salvar() {
    const nome = document.getElementById('nome').value;
    const email = document.getElementById('email').value;
    const novaSenha = document.getElementById('novaSenha').value;
    const funcao = document.getElementById('funcao').value;

    // Valida se os campos obrigatórios estão preenchidos
    if (!nome || !email) {
        mostrarMensagem('Nome e e-mail são obrigatórios.', 'danger');
        return;
    }

    // Coleta os esportes selecionados nos checkboxes
    const checkboxes = document.querySelectorAll('#checkboxEsportes input[type="checkbox"]:checked');
    const esportesSelecionados = Array.from(checkboxes).map(cb => ({ id: cb.value }));

    // Monta o objeto com os dados atualizados
    const usuarioAtualizado = {
        nome: nome,
        email: email,
        senha: novaSenha || usuario.senha,
        perfil: usuario.perfil,
        funcao: funcao,
        esportes: esportesSelecionados
    };

    // Envia as alterações para a API via PUT
    fetch(`/usuarios/${usuario.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(usuarioAtualizado)
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Erro ao salvar');
            }
        })
        .then(usuarioSalvo => {
            // Atualiza o localStorage com os novos dados
            localStorage.setItem('usuarioLogado', JSON.stringify(usuarioSalvo));
            mostrarMensagem('Perfil atualizado com sucesso!', 'success');

            // Atualiza os dados exibidos no card lateral
            document.getElementById('avatarInicial').textContent = usuarioSalvo.nome.charAt(0).toUpperCase();
            document.getElementById('perfilNome').textContent = usuarioSalvo.nome;
            document.getElementById('perfilEmail').textContent = usuarioSalvo.email;
            document.getElementById('perfilFuncao').textContent = usuarioSalvo.funcao || 'Atleta';
            document.getElementById('nomeUsuario').textContent = usuarioSalvo.nome;
            exibirEsportesPerfil(usuarioSalvo.esportes);
        })
        .catch(error => {
            mostrarMensagem('Erro ao atualizar perfil. Tente novamente.', 'danger');
        });
}

// Exibe uma mensagem de alerta na tela
function mostrarMensagem(texto, tipo) {
    const div = document.getElementById('mensagem');
    div.innerHTML = `<div class="alert alert-${tipo}">${texto}</div>`;
}