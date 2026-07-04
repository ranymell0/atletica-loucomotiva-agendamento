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

// Instância do modal de exclusão
const modalExcluir = new bootstrap.Modal(document.getElementById('modalExcluir'));

// ID do usuário a ser excluído
let idParaExcluir = null;

// Carrega a lista de usuários ao abrir a página
carregarUsuarios();

// Busca todos os usuários na API e preenche a tabela
function carregarUsuarios() {
    fetch('/usuarios')
        .then(response => response.json())
        .then(usuarios => {
            const tbody = document.getElementById('tabelaUsuarios');

            // Exibe mensagem se não houver usuários cadastrados
            if (usuarios.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">Nenhum usuário cadastrado.</td></tr>';
                return;
            }

            // Preenche a tabela com os usuários
            tbody.innerHTML = usuarios.map(u => `
                <tr>
                    <td>${u.nome}</td>
                    <td>${u.email}</td>
                    <td>${u.perfil === 'ADMIN'
                ? '<span class="badge bg-danger">ADMIN</span>'
                : '<span class="badge bg-secondary">ATLETA</span>'}
                    </td>
                    <td>
                        <button onclick="abrirModalExcluir('${u.id}', '${u.nome}')" class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-trash"></i> Excluir
                        </button>
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => {
            console.error('Erro ao carregar usuários:', error);
        });
}

// Abre o modal de confirmação de exclusão
function abrirModalExcluir(id, nome) {
    idParaExcluir = id;
    document.getElementById('nomeExcluir').textContent = nome;
    modalExcluir.show();
}

// Confirma e executa a exclusão do usuário
function confirmarExclusao() {
    fetch(`/usuarios/${idParaExcluir}`, { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                modalExcluir.hide();
                carregarUsuarios();
            }
        })
        .catch(error => {
            console.error('Erro ao excluir usuário:', error);
        });
}