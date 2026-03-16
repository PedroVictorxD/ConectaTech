# ConectaTech – Contexto Completo do Projeto

> **Último update:** 2026-03-16
> **Status:** Recomeçando do zero com Git Flow profissional

---

## 1. Visão Geral

**ConectaTech** é uma plataforma que interliga vagas de estágio com estudantes da UNP (Universidade Potiguar) em Mossoró/RN.

- **Backend:** Java 17 + Spring Boot 3.2.5 + PostgreSQL
- **Mobile:** React Native + Context API
- **Repositórios separados:** `conectatech-backend` e `conectatech-mobile`

---

## 2. Estrutura Git Flow

| Branch | Função |
|--------|--------|
| `main` | Versão estável/produção |
| `develop` | Desenvolvimento contínuo |
| `feature/<nome>` | Uma feature por branch |
| `release/<versão>` | Preparação de release |
| `hotfix/<nome>` | Correções urgentes em produção |

**Fluxo:**
1. Commit inicial na `main` (esqueleto do projeto)
2. Criar `develop` a partir da `main`
3. Cada feature em branch separada → merge para `develop`
4. Releases e hotfixes seguindo Git Flow

**Padrão de commits (Conventional Commits):**
- `feat:` nova funcionalidade
- `fix:` correção de bug
- `chore:` manutenção/configuração
- `docs:` documentação
- `refactor:` refatoração
- `test:` adição/correção de testes

---

## 3. Backend – Endpoints REST

### Autenticação
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/auth/login` | Login do usuário |
| POST | `/api/auth/recuperar-senha` | Recuperação de senha |
| PUT | `/api/auth/alterar-senha` | Alteração de senha |

### Vagas (Estudante)
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/vagas` | Listar vagas unificadas (Jooble + JSoup + Admin) |
| POST | `/api/vagas/selecionar` | Registrar vaga selecionada |
| GET | `/api/vagas/minhas` | Minhas vagas selecionadas |
| DELETE | `/api/vagas/minhas/{id}` | Remover vaga selecionada |

### Perfil (Estudante)
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/meus-dados` | Dados do usuário logado |
| PUT | `/api/meus-dados` | Atualizar dados |

### Admin
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/admin/usuarios` | Listar todos os usuários |
| PUT | `/api/admin/usuarios/{id}` | Atualizar usuário |
| GET | `/api/admin/vagas` | Listar todas as vagas |
| POST | `/api/admin/vagas` | Criar vaga |
| PUT | `/api/admin/vagas/{id}` | Atualizar vaga |
| DELETE | `/api/admin/vagas/{id}` | Remover vaga |

---

## 4. Backend – Estrutura de Pacotes

```
br.com.unp.conectatech/
├── ConectaTechApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── CorsConfig.java
├── security/
│   ├── JwtUtil.java
│   └── JwtAuthFilter.java
├── controller/
│   ├── AuthController.java
│   ├── VagaController.java
│   ├── UsuarioController.java
│   └── AdminController.java
├── service/
│   ├── AuthService.java
│   ├── VagaService.java
│   ├── UsuarioService.java
│   ├── JoobleService.java
│   └── ScraperService.java
├── repository/
│   ├── UsuarioRepository.java
│   ├── VagaRepository.java
│   └── VagaSelecionadaRepository.java
├── model/
│   ├── Usuario.java
│   ├── Vaga.java
│   ├── VagaSelecionada.java
│   ├── Role.java (enum)
│   └── FonteVaga.java (enum)
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RecuperarSenhaRequest.java
│   ├── AlterarSenhaRequest.java
│   ├── AtualizarUsuarioRequest.java
│   ├── UsuarioDTO.java
│   └── VagaDTO.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java
```

---

## 5. Backend – Dependências (pom.xml)

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- PostgreSQL Driver
- JWT (jjwt-api, jjwt-impl, jjwt-jackson) v0.12.5
- JSoup v1.17.2
- Lombok
- Spring Boot Starter Test + Spring Security Test
- H2 (testes)

---

## 6. Backend – Configurações (application.properties)

- **Server:** porta 8080
- **PostgreSQL:** localhost:5432/conectatech, user/pass: postgres
- **JPA:** ddl-auto=update, show-sql=true
- **JWT:** secret + expiração 24h
- **Jooble API:** URL + chave
- **Scraper:** cron diário 6h, habilitado
- **Logging:** DEBUG para br.com.unp.conectatech

---

## 7. Mobile – React Native (futuro)

### Telas
- Login, Recuperar/Alterar Senha
- Home (vagas em cards)
- Minhas Vagas
- Meus Dados
- Sobre Nós
- Admin (gerenciar usuários e vagas)

### Stack
- React Native + Context API
- Consumo REST da API backend
- UI/UX premium com design moderno

---

## 8. Features Git Flow (ordem de implementação)

### Backend
1. `feature/models` → Models, DTOs, application.properties
2. `feature/auth` → Security, JWT, AuthController
3. `feature/vagas` → VagaController, VagaSelecionada
4. `feature/admin` → AdminController
5. `feature/jooble-integration` → JoobleService
6. `feature/jsoup-scraper` → ScraperService

### Mobile (futuro)
1. `feature/login` → Telas de auth
2. `feature/vagas` → Home + Minhas Vagas
3. `feature/admin` → Painel admin

---

## 9. Funcionalidades

### Estudante UNP
- Login com JWT
- Recuperar/Alterar senha
- Ver perfil e editar dados
- Listar vagas de estágio (Jooble + JSoup + internas)
- Selecionar/deselecionar vagas
- Tela "Sobre Nós"

### Administrador
- Gerenciar estudantes (listar, editar)
- CRUD completo de vagas
- Monitorar vagas selecionadas
- Verificar fontes das vagas

---

## 10. Melhorias Futuras
- Notificações push
- Filtros avançados (curso, período, área)
- Histórico de vagas
- Multi-idioma
- Testes unitários e integração
- Microserviços
- Métricas de uso
