// URL Base da API REST para Parceiros
const API_BASE_URL = 'http://localhost:8080/api/parceiros';
// URL Base da API REST para Usuários (Registro)
const USER_API_URL = 'http://localhost:8080/api/usuarios';
// URL da API REST para Autenticação (Login)
const AUTH_API_URL = 'http://localhost:8080/api/auth';
// URL da API REST para Relatórios
const RELATORIO_API_URL = 'http://localhost:8080/api/relatorios';


// --- Funções de Utilidade e Autenticação ---
let idParaExclusao = null;
// Chave para armazenar o status de login no localStorage
const LOGIN_STATUS_KEY = 'loggedIn';
const EXPIRATION_KEY = 'loginExpiration';
// Duração do token simulado (1 hora em milissegundos)
const EXPIRATION_DURATION = 60 * 60 * 1000; 

/**
 * Exibe uma mensagem de sucesso ou erro na tela.
 */
function showMessage(message, type) {
    const box = document.getElementById('message-box');
    if (!box) return;

    box.textContent = message;
    box.className = `message-box message-${type}`;
    box.style.display = 'block';

    setTimeout(() => {
        box.style.display = 'none';
    }, 5000);
}

/**
 * Função para simular o logout (limpar localStorage e redirecionar)
 */
function logout() {
    localStorage.removeItem(LOGIN_STATUS_KEY);
    localStorage.removeItem(EXPIRATION_KEY);
    window.location.href = 'login.html';
}

/**
 * Verifica se o usuário está logado e se a sessão não expirou. 
 * Se não estiver ou expirou, redireciona para o login.
 */
function checkAuth() {
    const path = window.location.pathname;
    const isLoggedIn = localStorage.getItem(LOGIN_STATUS_KEY) === 'true';
    const expirationTime = localStorage.getItem(EXPIRATION_KEY);
    const now = Date.now();
    
    // Verifica se a sessão expirou
    if (isLoggedIn && expirationTime && now > parseInt(expirationTime)) {
        // Se expirou, força o logout e limpa
        logout();
        return;
    }

    if (isLoggedIn && (path === '/' || path.endsWith('/'))) {
         window.location.href = 'index.html';
         return;
    }
    // -------------------------------------------------------------------------

    // Se estiver na página de login ou registro
    if (path.endsWith('login.html') || path.endsWith('register.html')) {
        // Se já estiver logado (e não expirou), redireciona para o dashboard
        if (isLoggedIn) {
            window.location.href = 'index.html';
        }
        return;
    }
    
    // Para todas as outras páginas, exige autenticação
    if (!isLoggedIn) {
        window.location.href = 'login.html';
    }
}


// --- Manipuladores de Formulário e Ações ---

/**
 * Lida com o login do usuário.
 */
async function handleLogin() {
    const form = document.getElementById('login-form');
    if (!form) return;

    form.addEventListener('submit', async function(event) {
        event.preventDefault();
        const username = form.username.value;
        const password = form.password.value;
        
        if (!username || !password) {
            showMessage('Por favor, preencha todos os campos.', 'error');
            return;
        }

        try {
            // Requisição POST para o endpoint de login
            const response = await fetch(`${AUTH_API_URL}/login`, { 
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                // Login bem-sucedido: Salva o status e a expiração
                const expiration = Date.now() + EXPIRATION_DURATION;
                localStorage.setItem(LOGIN_STATUS_KEY, 'true');
                localStorage.setItem(EXPIRATION_KEY, expiration);
                window.location.href = 'index.html'; // Redireciona
            } else {
                // Login falhou (Usuário ou senha inválidos, 401 Unauthorized, etc.)
                localStorage.removeItem(LOGIN_STATUS_KEY);
                localStorage.removeItem(EXPIRATION_KEY);
                
                // Tenta exibir a mensagem de erro do backend
                const errorText = await response.text();
                showMessage(`Falha no Login: ${errorText || 'Credenciais inválidas.'}`, 'error');
            }
        } catch (error) {
            console.error('Erro de rede/conexão:', error);
            showMessage('Erro ao conectar com o servidor. Verifique se o backend está rodando.', 'error');
        }
    });
}

/**
 * Lida com o registro de um novo usuário.
 */
async function handleRegister() {
    const form = document.getElementById('register-form');
    if (!form) return;

    form.addEventListener('submit', async function(event) {
        event.preventDefault();
        const username = form.username.value;
        const password = form.password.value;
        const confirmPassword = form['confirm-password'].value;
        
        if (password !== confirmPassword) {
            showMessage('As senhas não coincidem.', 'error');
            return;
        }

        if (username.length < 4 || password.length < 6) {
             showMessage('Usuário deve ter no mínimo 4 caracteres e Senha no mínimo 6.', 'error');
            return;
        }

        try {
            const response = await fetch(`${USER_API_URL}/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            const responseText = await response.text();

            if (response.ok) {
                localStorage.removeItem(LOGIN_STATUS_KEY);
                localStorage.removeItem(EXPIRATION_KEY);
                
                showMessage('Registro bem-sucedido! Redirecionando para o login...', 'success');
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 2000);
            } else if (response.status === 409) {
                 showMessage('Nome de usuário já existe.', 'error');
                 localStorage.removeItem(LOGIN_STATUS_KEY);
                 localStorage.removeItem(EXPIRATION_KEY);
            } else {
                showMessage(`Erro no Registro: ${responseText || 'Erro desconhecido.'}`, 'error');
                localStorage.removeItem(LOGIN_STATUS_KEY);
                localStorage.removeItem(EXPIRATION_KEY);
            }
        } catch (error) {
            console.error('Erro de rede/conexão:', error);
            showMessage('Erro ao conectar com o servidor.', 'error');
            localStorage.removeItem(LOGIN_STATUS_KEY);
            localStorage.removeItem(EXPIRATION_KEY);
        }
    });
}


// --- Funções de Parceiros (CRUD) ---


let idParaEdicaoUrl = new URLSearchParams(window.location.search).get('id');
let tipoParceiro = new URLSearchParams(window.location.search).get('tipo');


/**
 * Salva (cria ou atualiza) um parceiro.
 */
async function salvarParceiro(event) {
    event.preventDefault();
    
    const id = document.getElementById('id').value;
    const nomeOuRazaoSocial = document.getElementById('nomeOuRazaoSocial').value;
    const documento = document.getElementById('documento').value;
    const tipo = document.getElementById('tipo').value;
    const email = document.getElementById('email').value;
    const telefone = document.getElementById('telefone').value;
    const observacoes = document.getElementById('observacoes').value;
    
    const isEditing = id !== null && id !== '';
    const url = isEditing ? `${API_BASE_URL}/${id}` : API_BASE_URL;
    const method = isEditing ? 'PUT' : 'POST';

    const parceiroData = {
        nomeOuRazaoSocial,
        documento,
        tipo,
        email,
        telefone,
        observacoes
    };

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(parceiroData)
        });

        if (response.ok) {
            const tipoMsg = tipo === 'CLIENTE' ? 'Cliente' : 'Fornecedor';
            showMessage(`${tipoMsg} ${isEditing ? 'atualizado' : 'cadastrado'} com sucesso!`, 'success');
            
            if (!isEditing) {
                 document.getElementById('parceiro-form').reset();
            }
            
            setTimeout(() => {
                const listPage = tipo === 'CLIENTE' ? 'lista_cliente.html' : 'lista_fornecedor.html';
                window.location.href = listPage;
            }, 2000);
            
        } else {
            const errorText = await response.text();
            showMessage(`Erro ao salvar: ${errorText || 'Erro desconhecido.'}`, 'error');
        }

    } catch (error) {
        console.error('Erro de rede/conexão:', error);
        showMessage('Erro ao conectar com o servidor.', 'error');
    }
}

/**
 * Carrega os dados de um parceiro para edição.
 */
async function loadParceiroParaEdicao(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/id/${id}`);
        if (!response.ok) {
            throw new Error('Parceiro não encontrado.');
        }
        const parceiro = await response.json();

        // Preenche o formulário
        document.getElementById('id').value = parceiro.id;
        document.getElementById('nomeOuRazaoSocial').value = parceiro.nomeOuRazaoSocial;
        document.getElementById('documento').value = parceiro.documento;
        document.getElementById('tipo').value = parceiro.tipo;
        document.getElementById('email').value = parceiro.email;
        document.getElementById('telefone').value = parceiro.telefone;
        document.getElementById('observacoes').value = parceiro.observacoes;
        
        // Ajusta títulos
        const tipoMsg = parceiro.tipo === 'CLIENTE' ? 'Cliente' : 'Fornecedor';
        document.getElementById('page-title').textContent = `Edição de ${tipoMsg}`;
        document.getElementById('form-title').textContent = `Editar ${tipoMsg}`;
        document.getElementById('save-btn').innerHTML = '<i class="fas fa-edit"></i> Atualizar Parceiro';


    } catch (error) {
        console.error('Erro ao carregar parceiro:', error);
        showMessage('Erro ao carregar dados do parceiro para edição.', 'error');
    }
}

function editarParceiro(id) {
    window.location.href = `cadastro.html?id=${id}`;
}

/**
 * Renderiza a tabela de parceiros.
 */
function renderParceiros(parceiros, tableBody) {
    tableBody.innerHTML = ''; // Limpa o "Carregando..."
    const formatter = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

    parceiros.forEach(parceiro => {
        const row = tableBody.insertRow();
        row.innerHTML = `
            <td>${parceiro.id}</td>
            <td>${parceiro.nomeOuRazaoSocial}</td>
            <td>${parceiro.documento}</td>
            <td>${parceiro.email || ''}</td>
            <td>${parceiro.telefone || ''}</td>
            <td>${parceiro.observacoes || ''}</td>
            <td>${parceiro.dataHoraCadastro ? formatter.format(new Date(parceiro.dataHoraCadastro)) : ''}</td>
            <td>
                <button class="btn btn-warning btn-sm" onclick="editarParceiro(${parceiro.id})"><i class="fas fa-edit"></i> Editar</button>
                <button class="btn btn-danger btn-sm" onclick="confirmarExclusao(${parceiro.id}, '${parceiro.nomeOuRazaoSocial}')"><i class="fas fa-trash-alt"></i> Excluir</button>
            </td>
        `;
    });
}


/**
 * Carrega a lista de parceiros (Clientes ou Fornecedores).
 * @param {string} tipoParceiro O tipo de parceiro ('CLIENTE' ou 'FORNECEDOR').
 */
async function loadParceiros(tipoParceiro) {
    if (!tipoParceiro) {
        showMessage('Tipo de parceiro não especificado.', 'error');
        return;
    }

    const tableBody = document.getElementById('parceiros-table-body');
    if (!tableBody) return;

    const tipoCaps = tipoParceiro.toUpperCase();
    
    // DEFINA AS VARIÁVEIS NO PLURAL E NO SINGULAR
    const tipoMsgPlural = tipoCaps === 'CLIENTE' ? 'Clientes' : 'Fornecedores';
    const tipoMsgSingular = tipoCaps === 'CLIENTE' ? 'Cliente' : 'Fornecedor'; 
    
    if (document.getElementById('page-title')) document.getElementById('page-title').textContent = `Gerenciar ${tipoMsgPlural}`;
    if (document.getElementById('form-title')) document.getElementById('form-title').textContent = `Lista de ${tipoMsgPlural}`;
    
    const linkCadastro = document.getElementById('link-cadastro');
    if (linkCadastro) {
        linkCadastro.href = `cadastro.html?tipo=${tipoCaps}`;
        linkCadastro.textContent = `Novo(a) ${tipoMsgSingular}`; 
    }
    
    tableBody.innerHTML = `<tr><td colspan="8" style="text-align: center;">Carregando...</td></tr>`;

    try {
        const endpointTipo = tipoCaps === 'CLIENTE' ? 'clientes' : 'fornecedores';
        const url = `${API_BASE_URL}/${endpointTipo}`;
        
        const response = await fetch(url);
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erro HTTP ${response.status}: ${errorText || response.statusText}`); 
        }
        
        const parceiros = await response.json();
        
        if (parceiros.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="8" style="text-align: center;">Nenhum ${tipoMsgSingular} cadastrado.</td></tr>`;
        } else {
            renderParceiros(parceiros, tableBody);
        }

    } catch (error) {
        console.error('Erro ao carregar parceiros:', error);
        const detailedErrorMsg = `Falha ao carregar dados. Detalhes: ${error.message.substring(0, 150)}...`;
        tableBody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: red;">${detailedErrorMsg}</td></tr>`;
        showMessage('Erro ao carregar a lista de parceiros. Verifique o console do navegador e os detalhes na tabela.', 'error');
    }
}

/**
 * Exibe o modal de confirmação de exclusão.
 */
function confirmarExclusao(id, nome) {
    idParaExclusao = id;
    document.getElementById('delete-nome').textContent = nome;
    document.getElementById('delete-modal').style.display = 'block';
}

/**
 * Executa a exclusão do parceiro.
 */
async function executarExclusaoParceiro() {
    if (!idParaExclusao) return;

    try {
        const response = await fetch(`${API_BASE_URL}/${idParaExclusao}`, {
            method: 'DELETE'
        });

        document.getElementById('delete-modal').style.display = 'none';
        
        if (response.ok) {
            showMessage('Parceiro excluído com sucesso!', 'success');
            
            // Recarrega a lista após a exclusão (Infere o tipo da URL atual)
            const path = window.location.pathname;
            let tipo = null;
            if (path.endsWith('lista_cliente.html')) {
                tipo = 'CLIENTE';
            } else if (path.endsWith('lista_fornecedor.html')) {
                tipo = 'FORNECEDOR';
            }
            if (tipo) {
                loadParceiros(tipo); 
            }

        } else {
            const errorText = await response.text();
            showMessage(`Erro ao excluir: ${errorText || 'Erro desconhecido.'}`, 'error');
        }

    } catch (error) {
        console.error('Erro de rede/conexão na exclusão:', error);
        document.getElementById('delete-modal').style.display = 'none';
        showMessage('Erro ao conectar com o servidor para exclusão.', 'error');
    } finally {
        idParaExclusao = null;
    }
}

/**
 * Carrega dados do dashboard (contagem de clientes e fornecedores)
 */
async function loadDashboardData() {
    try {
        const response = await fetch(`${API_BASE_URL}/relatorio`);
        if (!response.ok) {
             throw new Error('Erro ao buscar relatório.');
        }
        const relatorio = await response.json();
        
        document.getElementById('clientes-count').textContent = relatorio.clientes;
        document.getElementById('fornecedores-count').textContent = relatorio.fornecedores;
        document.getElementById('total-count').textContent = relatorio.total;
        
    } catch (error) {
        console.error('Erro ao carregar dados do dashboard:', error);
    }
}

/**
 * Configura o botão de geração de relatório PDF.
 */
function setupRelatorioButton() {
    const btn = document.getElementById('btn-relatorio-pdf');
    if (btn) {
        btn.addEventListener('click', gerarRelatorioPdf);
    }
}

/**
 * Chama o endpoint da API para gerar e baixar o PDF.
 */
async function gerarRelatorioPdf() {
    const btn = document.getElementById('btn-relatorio-pdf');
    if (!btn) return;
    
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Gerando PDF...';

    try {
        const url = `${RELATORIO_API_URL}/parceiros-pdf`;
        const response = await fetch(url);
        
        if (!response.ok) {
            let errorMessage = 'Falha ao gerar o relatório PDF.';
            try {
                const errorText = await response.text();
                if (errorText.length > 0) {
                     errorMessage += ` Detalhes: ${errorText.substring(0, 100)}...`;
                }
            } catch (e) {
                // Não foi possível ler o corpo do erro
            }
            throw new Error(errorMessage);
        }

        const blob = await response.blob(); 
        
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = 'relatorio_parceiros.pdf'; 
        if (contentDisposition && contentDisposition.indexOf('filename=') !== -1) {
            filename = contentDisposition.split('filename=')[1].replace(/"/g, '');
        }
        
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(downloadUrl);
        
        showMessage('Relatório PDF gerado com sucesso! O download deve ter iniciado.', 'success');

    } catch (error) {
        console.error('Erro ao gerar relatório:', error);
        showMessage(`Erro ao gerar relatório: ${error.message}`, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}


// --- Inicialização ---

document.addEventListener('DOMContentLoaded', () => {
    
    // VERIFICA AUTENTICAÇÃO PRIMEIRO
    checkAuth(); 
    
    const path = window.location.pathname;

    //Adiciona handleLogin() de volta para garantir que o formulário funcione
    if (path.endsWith('login.html')) {
        handleLogin();
    } else if (path.endsWith('register.html')) {
        handleRegister(); 
    } else if (path.endsWith('index.html')) { // Funciona corretamente após o redirecionamento de '/'
        loadDashboardData(); 
        setupRelatorioButton(); 
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) logoutBtn.addEventListener('click', logout);
    } else if (path.endsWith('lista_cliente.html') || path.endsWith('lista_fornecedor.html')) {
        
        let tipo = path.endsWith('lista_cliente.html') ? 'CLIENTE' : 'FORNECEDOR';
        
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) logoutBtn.addEventListener('click', logout);
        
        const closeModal = document.querySelector('.close-modal');
        if (closeModal) closeModal.addEventListener('click', () => { document.getElementById('delete-modal').style.display = 'none'; idParaExclusao = null; });
        const btnCancelDelete = document.getElementById('btn-cancel-delete');
        if (btnCancelDelete) btnCancelDelete.addEventListener('click', () => { document.getElementById('delete-modal').style.display = 'none'; idParaExclusao = null; });
        const btnConfirmDelete = document.getElementById('btn-confirm-delete');
        if (btnConfirmDelete) btnConfirmDelete.addEventListener('click', executarExclusaoParceiro);

        loadParceiros(tipo); 

    } else if (path.endsWith('cadastro.html')) {
        document.getElementById('parceiro-form').addEventListener('submit', salvarParceiro);
        
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) logoutBtn.addEventListener('click', logout);
        
        if (idParaEdicaoUrl) {
            loadParceiroParaEdicao(idParaEdicaoUrl);
        } else if (tipoParceiro) {
             const tipoMsg = tipoParceiro.toUpperCase() === 'CLIENTE' ? 'Cliente' : 'Fornecedor';
             document.getElementById('page-title').textContent = `Cadastro de ${tipoMsg}`;
             document.getElementById('form-title').textContent = `Novo Cadastro de ${tipoMsg}`;
             document.getElementById('tipo').value = tipoParceiro.toUpperCase();
        }
    }
});

// Torna funções de ação globalmente acessíveis no HTML
window.confirmarExclusao = confirmarExclusao;
window.loadParceiros = loadParceiros;
window.loadParceiroParaEdicao = loadParceiroParaEdicao; 
window.logout = logout; 
window.editarParceiro = editarParceiro;