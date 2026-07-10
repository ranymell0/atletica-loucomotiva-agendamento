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
const modal = new bootstrap.Modal(document.getElementById('modalHorario'));

// Carrega os dados ao abrir a página
carregarEspacos();
carregarHorarios();

// Busca todos os espaços e preenche o select do modal
function carregarEspacos() {
    fetch('/espacos')
        .then(response => response.json())
        .then(espacos => {
            const select = document.getElementById('espaco');
            espacos.filter(e => e.ativo).forEach(e => {
                select.innerHTML += `<option value="${e.id}">${e.nome}</option>`;
            });
        })
        .catch(error => {
            console.error('Erro ao carregar espaços:', error);
        });
}

// Busca todos os horários na API e preenche a tabela
function carregarHorarios() {
    fetch('/horarios')
        .then(response => response.json())
        .then(horarios => {
            const tbody = document.getElementById('tabelaHorarios');

            // Exibe mensagem se não houver horários cadastrados
            if (horarios.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Nenhum horário cadastrado.</td></tr>';
                return;
            }

            // Preenche a tabela com os horários
            tbody.innerHTML = horarios.map(h => `
                <tr>
                    <td>${h.espaco.nome}</td>
                    <td>${formatarDia(h.diaSemana)}</td>
                    <td>${formatarHora(h.horaInicio)}</td>
                    <td>${formatarHora(h.horaFim)}</td>
                    <td>${h.ativo
                ? '<span class="badge bg-success">Ativo</span>'
                : '<span class="badge bg-secondary">Inativo</span>'}
                    </td>
                    <td>
                        <button onclick="editar('${h.id}', '${h.espaco.id}', '${h.diaSemana}', '${h.horaInicio}', '${h.horaFim}', '${h.ativo}')" class="btn btn-sm btn-outline-secondary me-1">
                            <i class="bi bi-pencil"></i> Editar
                        </button>
                        <button onclick="deletar('${h.id}')" class="btn btn-sm btn-outline-danger">
                            <i class="bi bi-trash"></i> Excluir
                        </button>
                    </td>
                </tr>
            `).join('');
        })
        .catch(error => {
            console.error('Erro ao carregar horários:', error);
        });
}

// Abre o modal para cadastrar um novo horário
function abrirModal() {
    document.getElementById('modalTitulo').textContent = 'Novo Horário';
    document.getElementById('horarioId').value = '';
    document.getElementById('espaco').value = '';
    document.getElementById('diaSemana').value = 'SEGUNDA';
    document.getElementById('horaInicio').value = '';
    document.getElementById('horaFim').value = '';
    document.getElementById('ativo').value = 'true';
    document.getElementById('mensagem').innerHTML = '';
    modal.show();
}

// Preenche o modal com os dados do horário para edição
function editar(id, espacoId, diaSemana, horaInicio, horaFim, ativo) {
    document.getElementById('modalTitulo').textContent = 'Editar Horário';
    document.getElementById('horarioId').value = id;
    document.getElementById('espaco').value = espacoId;
    document.getElementById('diaSemana').value = diaSemana;
    document.getElementById('horaInicio').value = horaInicio.substring(0, 5);
    document.getElementById('horaFim').value = horaFim.substring(0, 5);
    document.getElementById('ativo').value = ativo;
    document.getElementById('mensagem').innerHTML = '';
    modal.show();
}

// Salva ou atualiza um horário na API
function salvar() {
    const id = document.getElementById('horarioId').value;
    const espacoId = document.getElementById('espaco').value;
    const diaSemana = document.getElementById('diaSemana').value;
    const horaInicio = document.getElementById('horaInicio').value;
    const horaFim = document.getElementById('horaFim').value;
    const ativo = document.getElementById('ativo').value === 'true';

    // Valida campos obrigatórios
    if (!espacoId || !horaInicio || !horaFim) {
        document.getElementById('mensagem').innerHTML = '<div class="alert alert-danger">Preencha todos os campos obrigatórios.</div>';
        return;
    }

    const horario = {
        espaco: { id: espacoId },
        diaSemana,
        horaInicio,
        horaFim,
        ativo
    };

    // Define o método e a URL conforme cadastro ou edição
    const method = id ? 'PUT' : 'POST';
    const url = id ? `/horarios/${id}` : '/horarios';

    // Envia os dados para a API
    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(horario)
    })
        .then(response => {
            if (response.ok) {
                modal.hide();
                carregarHorarios();
            } else {
                document.getElementById('mensagem').innerHTML = '<div class="alert alert-danger">Erro ao salvar horário.</div>';
            }
        })
        .catch(error => {
            console.error('Erro ao salvar horário:', error);
        });
}

// Remove um horário da API
function deletar(id) {
    // Confirma antes de excluir
    if (!confirm('Deseja realmente excluir este horário?')) return;

    fetch(`/horarios/${id}`, { method: 'DELETE' })
        .then(response => {
            if (response.ok) {
                carregarHorarios();
            }
        })
        .catch(error => {
            console.error('Erro ao excluir horário:', error);
        });
}

// Formata a hora de HH:mm:ss para HH:mm
function formatarHora(hora) {
    return hora.substring(0, 5);
}

// Formata o dia da semana para exibição
function formatarDia(dia) {
    const dias = {
        'SEGUNDA': 'Segunda-feira',
        'TERCA': 'Terça-feira',
        'QUARTA': 'Quarta-feira',
        'QUINTA': 'Quinta-feira',
        'SEXTA': 'Sexta-feira',
        'SABADO': 'Sábado',
        'DOMINGO': 'Domingo'
    };
    return dias[dia] || dia;
}