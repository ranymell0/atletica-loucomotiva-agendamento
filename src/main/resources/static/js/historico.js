// Recupera o usuário logado do localStorage
const usuario = JSON.parse(localStorage.getItem('usuarioLogado'));

// Redireciona para o login se não houver usuário logado
if (!usuario) {
    window.location.href = 'login.html';
}

// Exibe o nome do usuário na navbar
document.getElementById('nomeUsuario').textContent = usuario.nome;

// Remove o usuário do localStorage ao sair
function logout() {
    localStorage.removeItem('usuarioLogado');
}

// Lista completa de agendamentos do usuário
let meusAgendamentos = [];

// Carrega o histórico ao abrir a página
carregarHistorico();

// Busca todos os agendamentos do usuário logado na API
function carregarHistorico() {
    fetch('/agendamentos')
        .then(response => response.json())
        .then(agendamentos => {
            // Filtra apenas os agendamentos do usuário logado
            meusAgendamentos = agendamentos.filter(a => a.usuario.id === usuario.id);
            renderizarTabela(meusAgendamentos);
        })
        .catch(error => {
            console.error('Erro ao carregar histórico:', error);
        });
}

// Filtra os agendamentos pelo status selecionado
function filtrarAgendamentos() {
    const status = document.getElementById('filtroStatus').value;

    // Exibe todos ou filtra pelo status selecionado
    const filtrados = status === 'TODOS'
        ? meusAgendamentos
        : meusAgendamentos.filter(a => a.status === status);

    renderizarTabela(filtrados);
}

// Renderiza a tabela com os agendamentos fornecidos
function renderizarTabela(agendamentos) {
    const tbody = document.getElementById('tabelaHistorico');

    // Exibe mensagem se não houver agendamentos
    if (agendamentos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Nenhum agendamento encontrado.</td></tr>';
        return;
    }

    // Preenche a tabela com os agendamentos
    tbody.innerHTML = agendamentos.map(a => `
        <tr>
            <td>${formatarData(a.data)}</td>
            <td>${formatarHora(a.horaInicio)} - ${formatarHora(a.horaFim)}</td>
            <td>${a.esporte.nome}</td>
            <td>${a.espaco.nome}</td>
            <td>${a.tipo === 'PREDEFINIDO'
        ? '<span class="badge bg-primary">Pré-definido</span>'
        : '<span class="badge bg-info">Livre</span>'}
            </td>
            <td>${badgeStatus(a.status)}</td>
            <td>
                ${a.status === 'PENDENTE' ? `
                    <button onclick="cancelar('${a.id}')" class="btn btn-sm btn-outline-danger">
                        <i class="bi bi-x-lg"></i> Cancelar
                    </button>
                ` : '—'}
            </td>
        </tr>
    `).join('');
}

// Cancela um agendamento pendente
function cancelar(id) {
    if (!confirm('Deseja realmente cancelar este agendamento?')) return;

    fetch(`/agendamentos/${id}/cancelar`, { method: 'PATCH' })
        .then(response => {
            if (response.ok) {
                carregarHistorico();
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

// Formata a hora de HH:mm:ss para HH:mm
function formatarHora(hora) {
    return hora.substring(0, 5);
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