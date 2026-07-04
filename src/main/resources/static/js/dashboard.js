// Recupera o usuário logado do localStorage
const usuario = JSON.parse(localStorage.getItem('usuarioLogado'));

// Redireciona para o login se não houver usuário logado
if (!usuario) {
    window.location.href = 'login.html';
}

// Exibe o nome do usuário na navbar e nas boas vindas
document.getElementById('nomeUsuario').textContent = usuario.nome;
document.getElementById('nomeBoasVindas').textContent = usuario.nome;

// Remove o usuário do localStorage ao sair
function logout() {
    localStorage.removeItem('usuarioLogado');
}

// Busca os agendamentos do usuário logado na API
fetch('/agendamentos')
    .then(response => response.json())
    .then(agendamentos => {

        // Filtra apenas os agendamentos do usuário logado
        const meus = agendamentos.filter(a => a.usuario.id === usuario.id);

        // Atualiza os cards de resumo
        document.getElementById('totalAgendamentos').textContent = meus.length;
        document.getElementById('agendamentosPendentes').textContent = meus.filter(a => a.status === 'PENDENTE').length;
        document.getElementById('agendamentosConfirmados').textContent = meus.filter(a => a.status === 'CONFIRMADO').length;

        const tbody = document.getElementById('tabelaAgendamentos');

        // Exibe mensagem se não houver agendamentos
        if (meus.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Nenhum agendamento encontrado.</td></tr>';
            return;
        }

        // Preenche a tabela com os agendamentos
        tbody.innerHTML = meus.map(a => `
            <tr>
                <td>${formatarData(a.data)}</td>
                <td>${formatarHora(a.horaInicio)} - ${formatarHora(a.horaFim)}</td>
                <td>${a.esporte.nome}</td>
                <td>${a.espaco.nome}</td>
                <td>${badgeStatus(a.status)}</td>
            </tr>
        `).join('');
    })
    .catch(error => {
        console.error('Erro ao carregar agendamentos:', error);
    });

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