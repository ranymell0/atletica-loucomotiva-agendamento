// Recupera o usuário logado do localStorage
const usuario = JSON.parse(localStorage.getItem('usuarioLogado'));

// Redireciona para o login se não houver usuário logado ou não for ADMIN
if (!usuario || usuario.perfil !== 'ADMIN') {
    window.location.href = 'login.html';
}

// Exibe o nome do usuário na navbar
document.getElementById('nomeUsuario').textContent = usuario.nome;

// Remove o usuário do localStorage ao sair
function logout() {
    localStorage.removeItem('usuarioLogado');
}

// Instância do modal do Bootstrap
const modal = new bootstrap.Modal(document.getElementById('modalEsporte'));

// Carrega a lista de esportes ao abrir a página
carregarEsportes();

// Busca todos os esportes na API e preenche a tabela
function carregarEsportes() {
    fetch('/esportes')
        .then(response => response.json())
        .then(esportes => {
            const tbody = document.getElementById('tabelaEsportes');

            // Exibe mensagem se não houver esportes cadastrados
            if (esportes.length === 0) {
                tbody.innerHTML = '<tr><td colspan="2" class="text-center text-muted">Nenhum esporte cadastrado.</td></tr>';
                return;
            }

            // Preenche a tabela com os esportes
            tbody.innerHTML = esportes.map(e => `
                <tr>
                    <td>${e.nome}</td>
                    <td>
                        <button onclick="editar('${e.id}', '${e.nome}')" class="btn btn-sm btn-outline-secondary me-1">
                            <i class="bi bi-pencil"></i> Editar
                        </button>
                        <button onclick="deletar('${e.id}')" class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-trash"></i> Excluir
                        </button>
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => {
            console.error('Erro ao carregar esportes:', error);
        });
}

// Abre o modal para cadastrar um novo esporte
function abrirModal() {
    document.getElementById('modalTitulo').textContent = 'Novo Esporte';
    document.getElementById('esporteId').value = '';
    document.getElementById('nome').value = '';
    document.getElementById('mensagem').innerHTML = '';
    modal.show();
}

// Preenche o modal com os dados do esporte para edição
function editar(id, nome) {
    document.getElementById('modalTitulo').textContent = 'Editar Esporte';
    document.getElementById('esporteId').value = id;
    document.getElementById('nome').value = nome;
    document.getElementById('mensagem').innerHTML = '';
    modal.show();
}

// Salva ou atualiza um esporte na API
function salvar() {
    const id = document.getElementById('esporteId').value;
    const nome = document.getElementById('nome').value;

    // Valida se o nome foi preenchido
    if (!nome) {
        document.getElementById('mensagem').innerHTML = '<div class="alert alert-danger">O nome é obrigatório.</div>';
        return;
    }

    const esporte = { nome };

    // Define o método e a URL conforme cadastro ou edição
    const method = id ? 'PUT' : 'POST';
    const url = id ? `/esportes/${id}` : '/esportes';

    // Envia os dados para a API
    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(esporte)
    })
        .then(response => {
            if (response.ok) {
                modal.hide();
                carregarEsportes();
            } else {
                document.getElementById('mensagem').innerHTML = '<div class="alert alert-danger">Erro ao salvar esporte.</div>';
            }
        })
        .catch(error => {
            console.error('Erro ao salvar esporte:', error);
        });
}

// Remove um esporte da API
function deletar(id) {
    // Confirma antes de excluir
    if (!confirm('Deseja realmente excluir este esporte?')) return;

    fetch(`/esportes/${id}`, { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                carregarEsportes();
            }
        })
        .catch(error => {
            console.error('Erro ao excluir esporte:', error);
        });
}