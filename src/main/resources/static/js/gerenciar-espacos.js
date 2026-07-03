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
const modal = new bootstrap.Modal(document.getElementById('modalEspaco'));

// Carrega a lista de espaços ao abrir a página
carregarEspacos();

// Busca todos os espaços na API e preenche a tabela
function carregarEspacos() {
    fetch('/espacos')
        .then(response => response.json())
        .then(espacos => {
            const tbody = document.getElementById('tabelaEspacos');

            // Exibe mensagem se não houver espaços cadastrados
            if (espacos.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">Nenhum espaço cadastrado.</td></tr>';
                return;
            }

            // Preenche a tabela com os espaços
            tbody.innerHTML = espacos.map(e => `
                <tr>
                    <td>${e.nome}</td>
                    <td>${e.capacidadeMaxima || '—'}</td>
                    <td>${e.ativo ? '<span class="badge bg-success">Ativo</span>' : '<span class="badge bg-secondary">Inativo</span>'}</td>
                    <td>
                        <button onclick="editar('${e.id}', '${e.nome}', '${e.capacidadeMaxima || ''}', '${e.ativo}')" class="btn btn-sm btn-outline-secondary me-1">
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
            console.error('Erro ao carregar espaços:', error);
        });
}

// Abre o modal para cadastrar um novo espaço
function abrirModal() {
    document.getElementById('modalTitulo').textContent = 'Novo Espaço';
    document.getElementById('espacoId').value = '';
    document.getElementById('nome').value = '';
    document.getElementById('capacidadeMaxima').value = '';
    document.getElementById('ativo').value = 'true';
    document.getElementById('mensagem').innerHTML = '';
    modal.show();
}

// Preenche o modal com os dados do espaço para edição
function editar(id, nome, capacidadeMaxima, ativo) {
    document.getElementById('modalTitulo').textContent = 'Editar Espaço';
    document.getElementById('espacoId').value = id;
    document.getElementById('nome').value = nome;
    document.getElementById('capacidadeMaxima').value = capacidadeMaxima;
    document.getElementById('ativo').value = ativo;
    document.getElementById('mensagem').innerHTML = '';
    modal.show();
}

// Salva ou atualiza um espaço na API
function salvar() {
    const id = document.getElementById('espacoId').value;
    const nome = document.getElementById('nome').value;
    const capacidadeMaxima = document.getElementById('capacidadeMaxima').value;
    const ativo = document.getElementById('ativo').value === 'true';

    // Valida se o nome foi preenchido
    if (!nome) {
        document.getElementById('mensagem').innerHTML = '<div class="alert alert-danger">O nome é obrigatório.</div>';
        return;
    }

    const espaco = { nome, capacidadeMaxima: capacidadeMaxima || null, ativo };

    // Define o método e a URL conforme cadastro ou edição
    const method = id ? 'PUT' : 'POST';
    const url = id ? `/espacos/${id}` : '/espacos';

    // Envia os dados para a API
    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(espaco)
    })
        .then(response => {
            if (response.ok) {
                modal.hide();
                carregarEspacos();
            } else {
                document.getElementById('mensagem').innerHTML = '<div class="alert alert-danger">Erro ao salvar espaço.</div>';
            }
        })
        .catch(error => {
            console.error('Erro ao salvar espaço:', error);
        });
}

// Remove um espaço da API
function deletar(id) {
    // Confirma antes de excluir
    if (!confirm('Deseja realmente excluir este espaço?')) return;

    fetch(`/espacos/${id}`, { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                carregarEspacos();
            }
        })
        .catch(error => {
            console.error('Erro ao excluir espaço:', error);
        });
}