const usuario = JSON.parse(localStorage.getItem('usuarioLogado'));

if (!usuario || usuario.perfil !== 'ADMIN') {
    window.location.href = 'login.html';
}

document.getElementById('nomeUsuario').textContent = usuario.nome;

function logout() {
    localStorage.removeItem('usuarioLogado');
}

// Carregar totais
Promise.all([
    fetch('/agendamentos').then(r => r.json()),
    fetch('/usuarios').then(r => r.json()),
    fetch('/espacos').then(r => r.json()),
    fetch('/esportes').then(r => r.json())
]).then(([agendamentos, usuarios, espacos, esportes]) => {
    document.getElementById('totalAgendamentos').textContent = agendamentos.length;
    document.getElementById('totalUsuarios').textContent = usuarios.length;
    document.getElementById('totalEspacos').textContent = espacos.length;
    document.getElementById('totalEsportes').textContent = esportes.length;

    const tbody = document.getElementById('tabelaAgendamentos');

    if (agendamentos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Nenhum agendamento encontrado.</td></tr>';
        return;
    }

    tbody.innerHTML = agendamentos.slice(0, 10).map(a => `
        <tr>
            <td>${a.usuario.nome}</td>
            <td>${formatarData(a.data)}</td>
            <td>${a.horaInicio} - ${a.horaFim}</td>
            <td>${a.esporte.nome}</td>
            <td>${a.espaco.nome}</td>
            <td>${badgeStatus(a.status)}</td>
            <td>
                ${a.status === 'PENDENTE' ? `
                    <button onclick="confirmar('${a.id}')" class="btn btn-success btn-sm">Confirmar</button>
                    <button onclick="cancelar('${a.id}')" class="btn btn-danger btn-sm">Cancelar</button>
                ` : '—'}
            </td>
        </tr>
    `).join('');
}).catch(error => {
    console.error('Erro ao carregar dados:', error);
});

function confirmar(id) {
    atualizarStatus(id, 'CONFIRMADO');
}

function cancelar(id) {
    atualizarStatus(id, 'CANCELADO');
}

function atualizarStatus(id, status) {
    fetch(`/agendamentos/${id}`)
        .then(r => r.json())
        .then(agendamento => {
            agendamento.status = status;
            return fetch(`/agendamentos/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(agendamento)
            });
        })
        .then(() => location.reload())
        .catch(error => console.error('Erro ao atualizar status:', error));
}

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