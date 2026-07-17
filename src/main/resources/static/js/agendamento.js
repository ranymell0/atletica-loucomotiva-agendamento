// Recupera o usuário logado do localStorage
const usuario = JSON.parse(localStorage.getItem('usuarioLogado'));

// Redireciona para o login se não houver usuário logado
if (!usuario) {
    window.location.href = 'login.html';
}

// Exibe o nome do usuário na navbar
document.getElementById('nomeUsuario').textContent = usuario.nome;

// Define para onde a página deve voltar/redirecionar conforme o perfil de quem está logado
const paginaDashboard = usuario.perfil === 'ADMIN' ? 'dashboard-admin.html' : 'dashboard.html';
document.getElementById('linkLogo').setAttribute('href', paginaDashboard);
document.getElementById('linkVoltar').setAttribute('href', paginaDashboard);

// Remove o usuário do localStorage ao sair
function logout() {
    localStorage.removeItem('usuarioLogado');
}

// Carrega os esportes ao abrir a página
carregarEsportes();

// Busca todos os esportes na API e preenche o select
function carregarEsportes() {
    fetch('/esportes')
        .then(response => response.json())
        .then(esportes => {
            const select = document.getElementById('esporte');
            esportes.forEach(e => {
                select.innerHTML += `<option value="${e.id}">${e.nome}</option>`;
            });
        })
        .catch(error => {
            console.error('Erro ao carregar esportes:', error);
        });
}

// Busca todos os espaços na API e preenche o select
function carregarEspacos() {
    fetch('/espacos')
        .then(response => response.json())
        .then(espacos => {
            const select = document.getElementById('espaco');
            select.innerHTML = '<option value="">Selecione um espaço</option>';

            // Filtra apenas espaços ativos
            const ativos = espacos.filter(e => e.ativo);
            ativos.forEach(e => {
                select.innerHTML += `<option value="${e.id}">${e.nome}</option>`;
            });
        })
        .catch(error => {
            console.error('Erro ao carregar espaços:', error);
        });
}

// Busca os horários disponíveis para o espaço e data selecionados
function carregarHorarios() {
    const espacoId = document.getElementById('espaco').value;
    const data = document.getElementById('data').value;
    const tipo = document.querySelector('input[name="tipo"]:checked').value;

    // Só carrega se for horário pré-definido e espaço e data estiverem selecionados
    if (tipo !== 'PREDEFINIDO' || !espacoId || !data) return;

    // Obtém o dia da semana da data selecionada
    const diasSemana = ['DOMINGO', 'SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO'];
    const diaSemana = diasSemana[new Date(data + 'T00:00:00').getDay()];

    fetch('/horarios')
        .then(response => response.json())
        .then(horarios => {
            const select = document.getElementById('horario');
            select.innerHTML = '<option value="">Selecione um horário</option>';

            // Filtra horários pelo espaço e dia da semana selecionados
            const disponiveis = horarios.filter(h =>
                h.espaco.id === espacoId &&
                h.diaSemana === diaSemana &&
                h.ativo
            );

            if (disponiveis.length === 0) {
                select.innerHTML += '<option disabled>Nenhum horário disponível</option>';
                return;
            }

            disponiveis.forEach(h => {
                select.innerHTML += `<option value="${h.id}" data-inicio="${h.horaInicio}" data-fim="${h.horaFim}">${h.horaInicio} - ${h.horaFim}</option>`;
            });
        })
        .catch(error => {
            console.error('Erro ao carregar horários:', error);
        });
}

// Altera os campos exibidos conforme o tipo de agendamento selecionado
function alterarTipo() {
    const tipo = document.querySelector('input[name="tipo"]:checked').value;

    if (tipo === 'PREDEFINIDO') {
        // Exibe campo de horário pré-definido e oculta campos de horário livre
        document.getElementById('campoHorarioPredefinido').classList.remove('d-none');
        document.getElementById('campoHoraInicio').classList.add('d-none');
        document.getElementById('campoHoraFim').classList.add('d-none');
    } else {
        // Oculta campo de horário pré-definido e exibe campos de horário livre
        document.getElementById('campoHorarioPredefinido').classList.add('d-none');
        document.getElementById('campoHoraInicio').classList.remove('d-none');
        document.getElementById('campoHoraFim').classList.remove('d-none');
    }
}

// Realiza o agendamento enviando os dados para a API
function agendar() {
    const tipo = document.querySelector('input[name="tipo"]:checked').value;
    const esporteId = document.getElementById('esporte').value;
    const espacoId = document.getElementById('espaco').value;
    const data = document.getElementById('data').value;

    // Valida campos obrigatórios
    if (!esporteId || !espacoId || !data) {
        mostrarMensagem('Por favor, preencha todos os campos obrigatórios.', 'danger');
        return;
    }

    let horarioId = null;
    let horaInicio = null;
    let horaFim = null;

    if (tipo === 'PREDEFINIDO') {
        // Obtém os dados do horário pré-definido selecionado
        const selectHorario = document.getElementById('horario');
        horarioId = selectHorario.value;

        if (!horarioId) {
            mostrarMensagem('Por favor, selecione um horário.', 'danger');
            return;
        }

        const option = selectHorario.options[selectHorario.selectedIndex];
        horaInicio = option.dataset.inicio;
        horaFim = option.dataset.fim;
    } else {
        // Obtém os dados do horário livre
        horaInicio = document.getElementById('horaInicio').value;
        horaFim = document.getElementById('horaFim').value;

        if (!horaInicio || !horaFim) {
            mostrarMensagem('Por favor, informe o horário de início e fim.', 'danger');
            return;
        }
    }

    // Monta o objeto de agendamento
    const agendamento = {
        usuario: { id: usuario.id },
        espaco: { id: espacoId },
        esporte: { id: esporteId },
        horario: horarioId ? { id: horarioId } : null,
        data: data,
        horaInicio: horaInicio,
        horaFim: horaFim,
        tipo: tipo,
        status: 'PENDENTE'
    };

    // Envia o agendamento para a API via POST
    fetch('/agendamentos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(agendamento)
    })
        .then(response => {
            if (response.ok) {
                mostrarMensagem('Agendamento realizado com sucesso!', 'success');
                // Redireciona para o dashboard correto (admin ou atleta) após 2 segundos
                setTimeout(() => {
                    window.location.href = paginaDashboard;
                }, 2000);
            } else {
                mostrarMensagem('Erro ao realizar agendamento. Tente novamente.', 'danger');
            }
        })
        .catch(error => {
            mostrarMensagem('Erro ao conectar com o servidor.', 'danger');
        });
}

// Exibe uma mensagem de alerta na tela
function mostrarMensagem(texto, tipo) {
    const div = document.getElementById('mensagem');
    div.innerHTML = `<div class="alert alert-${tipo}">${texto}</div>`;
}