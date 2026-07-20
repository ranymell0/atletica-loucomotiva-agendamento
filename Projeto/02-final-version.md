# CSI606-2026-01 - Remoto - Trabalho Final - Resultados

**Discente:** Rany Souza Melo - 23.1.8065

## Resumo

O **Atlética Loucomotiva Agendamento** foi desenvolvido como uma aplicação web Full-Stack para substituir o controle manual/informal de reservas de espaços esportivos da Atlética Loucomotiva (UFOP João Monlevade). O sistema permite cadastrar usuário, fazer login, acessar áreas protegidas com perfis distintos (atleta e administrador), agendar horários de treino e gerenciar espaços, modalidades e horários disponíveis.

A versão final centraliza as principais ações em uma interface web simples, com integração entre frontend, API REST e banco de dados relacional. O objetivo principal foi aplicar os conceitos da disciplina em um projeto completo, passando por rotas, controllers, camada de serviço, banco relacional via JPA/Hibernate e consumo de API via JavaScript puro, além de uma suíte de testes automatizados cobrindo várias camadas da aplicação.

## 1. Tecnologias utilizadas - Backend e Frontend

**Backend**

- Java 17 como linguagem principal.
- Spring Boot 3.5 para criação da API REST, seguindo a arquitetura em camadas (Controller/Service/Repository).
- Spring Data JPA (Hibernate) para modelagem e acesso ao banco de dados.
- PostgreSQL como banco de dados relacional.
- Maven para gerenciamento de dependências e build.
- JUnit 5 e Mockito para testes unitários, de componente e de sistema.

**Frontend**

- HTML5 e CSS3 para estrutura e estilização das telas.
- Bootstrap 5 e Bootstrap Icons para componentes visuais e responsividade.
- JavaScript puro (vanilla JS) para lógica de interface e consumo da API via `fetch`.
- `localStorage` do navegador para manter a sessão do usuário logado.

## 2. Funcionalidades implementadas

- Cadastro de usuário, com atribuição de perfil (ATLETA/ADMIN) decidida pelo servidor a partir de um código de administrador.
- Login de usuário via endpoint dedicado, sem expor a lista de usuários ou senhas ao navegador.
- Proteção de rotas privadas no frontend, com redirecionamento para o login quando não há sessão válida.
- Dashboards distintos para atleta e administrador.
- Cadastro, listagem, edição e remoção de agendamentos.
- Confirmação e cancelamento de agendamentos pelo administrador.
- Cadastro, listagem, edição e remoção de espaços esportivos, modalidades (esportes) e horários.
- Agendamento de horários por data, modalidade e espaço, com horário pré-definido ou horário livre.
- Agenda semanal do atleta, organizada por dia, com navegação entre semanas.
- Histórico de agendamentos do atleta, com filtro por status (pendente, confirmado, cancelado).
- Edição de perfil (dados pessoais, senha e modalidades esportivas praticadas), com navegação adaptada conforme o perfil do usuário logado.
- Validações de negócio no backend: e-mail duplicado, data de agendamento no passado, campos obrigatórios ausentes, horário inconsistente (hora de fim anterior à hora de início) e esporte não selecionado.

## 3. Funcionalidades previstas e não implementadas

- **Upload de foto de perfil:** estava sendo avaliado durante o desenvolvimento, mas foi descartado por decisão de escopo, priorizando o tempo disponível para consolidar as funcionalidades centrais e a suíte de testes.

## 4. Outras funcionalidades implementadas

- Endpoint dedicado de login (`POST /login`), criado durante o desenvolvimento para substituir uma abordagem inicial insegura que expunha a lista completa de usuários no navegador.
- Atribuição de perfil de administrador via código secreto validado no servidor, impedindo que um usuário se autopromovesse a admin manipulando a requisição.
- Navegação da interface adaptada dinamicamente conforme o perfil do usuário logado.
- Administrador também pode criar agendamentos diretamente pelo próprio painel.
- Suíte de testes automatizados cobrindo Service, Controller, integração com banco real e fluxos completos de sistema (ver seção 5).

## 5. Principais desafios e dificuldades

O principal desafio foi escrever testes que fossem além da simples verificação unitária, avançando para testes de componente (Controller → Service → Repository → banco PostgreSQL real, sem mocks) e testes de sistema (fluxos completos simulando o uso real por um atleta e por um administrador). Esse processo revelou falhas que não apareceriam em testes isolados, como:

- A mutação indevida de uma entidade JPA gerenciada durante um teste encadeado, que quase persistiu um valor `null` num campo `NOT NULL` do banco no meio de um fluxo de teste.
- Duas falhas de segurança reais: ausência de validação de e-mail duplicado no cadastro, e a possibilidade de qualquer usuário se autopromover a administrador simplesmente enviando `"perfil": "ADMIN"` no corpo da requisição.
- Uma abordagem de login originalmente insegura, que autenticava buscando todos os usuários (senhas incluídas) e comparando no navegador, corrigida com a criação de um endpoint de autenticação dedicado no backend.

Outra dificuldade foi adaptar telas que originalmente serviam só ao atleta (como perfil e agendamento) para funcionarem também para o administrador, sem duplicar arquivos HTML — resolvido com lógica condicional em JavaScript que ajusta links de navegação e visibilidade de botões conforme o perfil do usuário logado.

## 6. Instruções para instalação e execução

**Pré-requisitos**

- Java 17 instalado.
- PostgreSQL instalado ou uma instância PostgreSQL disponível.
- Maven (ou o wrapper `mvnw` incluso no projeto).

**Configuração do banco de dados**

Crie um banco de dados PostgreSQL e configure as credenciais em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agendamento
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
```

**Executando o backend (também serve o frontend)**

```bash
cd atletica-loucomotiva-agendamento
./mvnw spring-boot:run
```

Por padrão, a aplicação executa em:

```
http://localhost:8080
```

As páginas do frontend ficam acessíveis em `http://localhost:8080/pages/`, por exemplo:

```
http://localhost:8080/pages/login.html
```

**Executando os testes**

Os testes de componente e de sistema exigem um banco de dados de teste PostgreSQL configurado em `src/test/resources/application-test.properties`, além da variável de ambiente `DB_TEST_PASSWORD` com a senha desse banco. Após configurado, os testes podem ser executados via IntelliJ ou:

```bash
./mvnw test
```

## 7. Referências

SPRING. *Spring Boot Reference Documentation*. Disponível em: https://docs.spring.io/spring-boot/reference/. Acesso em: 17 jul. 2026.

SPRING. *Spring Framework Reference Documentation*. Disponível em: https://docs.spring.io/spring-framework/reference/. Acesso em: 17 jul. 2026.

POSTGRESQL. *PostgreSQL Documentation*. Disponível em: https://www.postgresql.org/docs/. Acesso em: 17 jul. 2026.

GETBOOTSTRAP. *Bootstrap Documentation*. Disponível em: https://getbootstrap.com/docs/. Acesso em: 17 jul. 2026.

SOMMERVILLE, Ian. *Engenharia de Software*. 10. ed. São Paulo: Pearson Education do Brasil, 2019.
