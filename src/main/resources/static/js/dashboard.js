const usuario = JSON.parse(localStorage.getItem('usuarioLogado'));

if (!usuario) {
    window.location.href = 'login.html';
}

document.getElementById('nomeUsuario').textContent = usuario.nome;
document.getElementById('nomeBoasVindas').textContent = usuario.nome;

function logout() {
    localStorage.removeItem('usuarioLogado');
}

fetch('/agendamentos')
    .then(response => response.json())
    .then(agendamentos => {
        const meus = agendamentos.filter(a => a.usuario.id === usuario.id);

        document.getElementById('totalAgendamentos').textContent = meus.length;
        document.getElementById('agendamentosPendentes').textContent = meus.filter(a => a.status === 'PENDENTE').length;
        document.getElementById('agendamentosConfirmados').textContent = meus.filter(a => a.status === 'CONFIRMADO').length;

        const tbody = document.getElementById('tabelaAgendamentos');

        if (meus.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Nenhum agendamento encontrado.</td></tr>';
            return;
        }

        tbody.innerHTML = meus.map(a => `
            <tr>
                <td>${formatarData(a.data)}</td>
                <td>${a.horaInicio} - ${a.horaFim}</td>
                <td>${a.esporte.nome}</td>
                <td>${a.espaco.nome}</td>
                <td>${badgeStatus(a.status)}</td>
            </tr>
        `).join('');
    })
    .catch(error => {
        console.error('Erro ao carregar agendamentos:', error);
    });

function formatarData(data) {
    const [ano, mes, dia] = data.split('-');
    return `${dia}/${mes}/${ano}`;
}

function badgeStatus(status) {
    const cores = {
        'PENDENTE': 'warning',
        'CONFIRMADO': 'success',
        'CANCELADO': 'danger'
    };
    return `<span class="badge bg-${cores[status] || 'secondary'}">${status}</span>`;
}