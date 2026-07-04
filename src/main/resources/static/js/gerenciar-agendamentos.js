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

// Lista completa de agendamentos carregados da API
let todosAgendamentos = [];

// Carrega a lista de agendamentos ao abrir a página
carregarAgendamentos();

// Busca todos os agendamentos na API e preenche a tabela
function carregarAgendamentos() {
    fetch('/agendamentos')
        .then(response => response.json())
        .then(agendamentos => {
            todosAgendamentos = agendamentos;
            renderizarTabela(agendamentos);
        })
        .catch(error => {
            console.error('Erro ao carregar agendamentos:', error);
        });
}

// Filtra os agendamentos pelo status selecionado
function filtrarAgendamentos() {
    const status = document.getElementById('filtroStatus').value;

    // Exibe todos ou filtra pelo status selecionado
    const filtrados = status === 'TODOS'
        ? todosAgendamentos
        : todosAgendamentos.filter(a => a.status === status);

    renderizarTabela(filtrados);
}

// Renderiza a tabela com os agendamentos fornecidos
function renderizarTabela(agendamentos) {
    const tbody = document.getElementById('tabelaAgendamentos');

    // Exibe mensagem se não houver agendamentos
    if (agendamentos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">Nenhum agendamento encontrado.</td></tr>';
        return;
    }

    // Preenche a tabela com os agendamentos
    tbody.innerHTML = agendamentos.map(a => `
        <tr>
            <td>${a.usuario.nome}</td>
            <td>${formatarData(a.data)}</td>
            <td>${a.horaInicio} - ${a.horaFim}</td>
            <td>${a.esporte.nome}</td>
            <td>${a.espaco.nome}</td>
            <td>${a.tipo === 'PREDEFINIDO'
        ? '<span class="badge bg-primary">Pré-definido</span>'
        : '<span class="badge bg-info">Livre</span>'}
            </td>
            <td>${badgeStatus(a.status)}</td>
            <td>
                ${a.status === 'PENDENTE' ? `
                    <button onclick="confirmar('${a.id}')" class="btn btn-sm btn-success me-1">
                        <i class="bi bi-check-lg"></i> Confirmar
                    </button>
                    <button onclick="cancelar('${a.id}')" class="btn btn-sm btn-danger">
                        <i class="bi bi-x-lg"></i> Cancelar
                    </button>
                ` : '—'}
            </td>
        </tr>
    `).join('');
}

// Confirma um agendamento via PATCH
function confirmar(id) {
    fetch(`/agendamentos/${id}/confirmar`, { method: 'PATCH' })
        .then(response => {
            if (response.ok) {
                carregarAgendamentos();
            }
        })
        .catch(error => {
            console.error('Erro ao confirmar agendamento:', error);
        });
}

// Cancela um agendamento via PATCH
function cancelar(id) {
    fetch(`/agendamentos/${id}/cancelar`, { method: 'PATCH' })
        .then(response => {
            if (response.ok) {
                carregarAgendamentos();
            }
        })
        .catch(error => {
            console.error('Erro ao cancelar agendamento:', error);
        });
}

// Formata a data de yyyy-mm-dd para dd/mm/yyyy
function formatarData(data) {
    const [ano, mes, dia] = data.split('-');
    return `${dia}/${mes}/${ano}`;
}

// Retorna um badge colorido conforme o status do agendamento
function badgeStatus(status) {
    const cores = {
        'PENDENTE': 'warning',
        'CONFIRMADO': 'success',
        'CANCELADO': 'danger'
    };
    return `<span class="badge bg-${cores[status] || 'secondary'}">${status}</span>`;
}