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

// Busca todos os dados necessários para o painel simultaneamente
Promise.all([
    fetch('/agendamentos').then(r => r.json()),
    fetch('/usuarios').then(r => r.json()),
    fetch('/espacos').then(r => r.json()),
    fetch('/esportes').then(r => r.json())
]).then(([agendamentos, usuarios, espacos, esportes]) => {

    // Atualiza os cards de resumo com os totais
    document.getElementById('totalAgendamentos').textContent = agendamentos.length;
    document.getElementById('totalUsuarios').textContent = usuarios.length;
    document.getElementById('totalEspacos').textContent = espacos.length;
    document.getElementById('totalEsportes').textContent = esportes.length;

    const tbody = document.getElementById('tabelaAgendamentos');

    // Exibe mensagem se não houver agendamentos
    if (agendamentos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Nenhum agendamento encontrado.</td></tr>';
        return;
    }

    // Preenche a tabela com os 10 últimos agendamentos
    tbody.innerHTML = agendamentos.slice(0, 10).map(a => `
        <tr>
            <td>${a.usuario.nome}</td>
            <td>${formatarData(a.data)}</td>
            <td>${formatarHora(a.horaInicio)} - ${formatarHora(a.horaFim)}</td>
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

// Confirma um agendamento pendente
function confirmar(id) {
    atualizarStatus(id, 'CONFIRMADO');
}

// Cancela um agendamento pendente
function cancelar(id) {
    atualizarStatus(id, 'CANCELADO');
}

// Atualiza o status de um agendamento via PUT
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