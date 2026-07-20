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

// Nomes dos dias da semana para exibição
const NOMES_DIAS = ['Domingo', 'Segunda-feira', 'Terça-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sábado'];

// Guarda o deslocamento de semanas em relação à semana atual (0 = semana atual)
let deslocamentoSemana = 0;

// Todos os agendamentos do usuário logado, carregados uma vez
let meusAgendamentos = [];

// Carrega os agendamentos do usuário ao abrir a página
carregarAgendamentos();

// Busca todos os agendamentos do usuário logado na API
function carregarAgendamentos() {
    fetch('/agendamentos')
        .then(response => response.json())
        .then(agendamentos => {
            meusAgendamentos = agendamentos.filter(a => a.usuario.id === usuario.id);
            renderizarSemana();
        })
        .catch(error => {
            console.error('Erro ao carregar agendamentos:', error);
        });
}

// Retorna a data de início (domingo) da semana com o deslocamento aplicado
function obterInicioSemana() {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const diaSemanaAtual = hoje.getDay();
    const inicio = new Date(hoje);
    inicio.setDate(hoje.getDate() - diaSemanaAtual + (deslocamentoSemana * 7));
    return inicio;
}

// Formata uma data (objeto Date) para dd/mm/yyyy
function formatarDataExibicao(data) {
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    return `${dia}/${mes}/${ano}`;
}

// Formata uma data (objeto Date) para yyyy-mm-dd, comparável com o campo "data" da API
function formatarDataISO(data) {
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    return `${ano}-${mes}-${dia}`;
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

// Navega para a semana anterior
function semanaAnterior() {
    deslocamentoSemana--;
    renderizarSemana();
}

// Volta para a semana atual
function semanaAtual() {
    deslocamentoSemana = 0;
    renderizarSemana();
}

// Navega para a próxima semana
function proximaSemana() {
    deslocamentoSemana++;
    renderizarSemana();
}

// Monta a lista de 7 dias a partir do início da semana e renderiza na tela
function renderizarSemana() {
    const inicioSemana = obterInicioSemana();
    const fimSemana = new Date(inicioSemana);
    fimSemana.setDate(inicioSemana.getDate() + 6);

    // Atualiza o texto do intervalo exibido no cabeçalho
    document.getElementById('intervaloSemana').textContent =
        `${formatarDataExibicao(inicioSemana)} a ${formatarDataExibicao(fimSemana)}`;

    const listaDias = document.getElementById('listaDias');
    listaDias.innerHTML = '';

    for (let i = 0; i < 7; i++) {
        const diaAtual = new Date(inicioSemana);
        diaAtual.setDate(inicioSemana.getDate() + i);
        const dataISO = formatarDataISO(diaAtual);

        // Filtra os agendamentos do usuário para este dia específico
        const agendamentosDoDia = meusAgendamentos
            .filter(a => a.data === dataISO)
            .sort((a, b) => a.horaInicio.localeCompare(b.horaInicio));

        const card = document.createElement('div');
        card.className = 'card p-4 mb-3';

        const cabecalho = `
            <h5 class="fw-bold mb-3">
                ${NOMES_DIAS[diaAtual.getDay()]}
                <span class="text-muted fw-normal">— ${formatarDataExibicao(diaAtual)}</span>
            </h5>
        `;

        let corpo;
        if (agendamentosDoDia.length === 0) {
            corpo = '<p class="text-muted mb-0">Nenhum agendamento neste dia.</p>';
        } else {
            corpo = agendamentosDoDia.map(a => `
                <div class="d-flex justify-content-between align-items-center py-2 border-bottom">
                    <div>
                        <strong>${formatarHora(a.horaInicio)} - ${formatarHora(a.horaFim)}</strong>
                        &nbsp;•&nbsp; ${a.esporte.nome}
                        &nbsp;•&nbsp; ${a.espaco.nome}
                    </div>
                    <div>${badgeStatus(a.status)}</div>
                </div>
            `).join('');
        }

        card.innerHTML = cabecalho + corpo;
        listaDias.appendChild(card);
    }
}