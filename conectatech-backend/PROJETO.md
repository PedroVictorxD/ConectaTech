# ConectaTech API

Plataforma que conecta estudantes de diversos cursos da UNP a vagas de estagio na regiao de Mossoro/RN.

## Stack

- **Java 17** + **Spring Boot 3.2.5**
- **Spring Security** com JWT (stateless)
- **Spring Data JPA** + **PostgreSQL** (producao) / **H2** (testes)
- **SpringDoc OpenAPI 2.5.0** (Swagger UI)
- **Lombok** para reducao de boilerplate
- **JSoup** para web scraping
- **Maven** como build tool

## Estrutura do Projeto

```
src/main/java/br/com/unp/conectatech/
├── ConectaTechApplication.java       # Entry point (@EnableScheduling)
├── config/
│   ├── OpenApiConfig.java            # Configuracao Swagger/OpenAPI
│   └── SecurityConfig.java           # Filtros de seguranca, CORS, rotas publicas/privadas
├── controller/
│   ├── AuthController.java           # /api/auth — registro, login, recuperacao de senha
│   ├── UsuarioController.java        # /api/profile — perfil do estudante autenticado
│   ├── VagaController.java           # /api/jobs — listagem, busca, selecao de vagas
│   └── AdminController.java          # /api/admin — gerenciamento de usuarios e vagas (ADMIN)
├── dto/
│   ├── RegistroRequest.java          # Body do POST /register
│   ├── LoginRequest.java             # Body do POST /login
│   ├── LoginResponse.java            # Resposta do login (token, nome, email, role)
│   ├── RecuperarSenhaRequest.java    # Body do POST /forgot-password
│   ├── AlterarSenhaRequest.java      # Body do PUT /reset-password
│   ├── AtualizarUsuarioRequest.java  # Body do PUT /profile
│   ├── SelecionarVagaRequest.java    # Body do POST /jobs/select
│   ├── UsuarioDTO.java               # Representacao do usuario nas respostas
│   ├── VagaDTO.java                  # Representacao da vaga nas respostas
│   ├── MessageResponse.java          # Resposta simples com campo "message"
│   └── ImportResponse.java           # Resposta de importacao (message + vagasImportadas)
├── model/
│   ├── Usuario.java                  # Entidade JPA — tabela "usuarios"
│   ├── Vaga.java                     # Entidade JPA — tabela "vagas"
│   ├── VagaSelecionada.java          # Entidade JPA — tabela "vagas_selecionadas" (N:N)
│   ├── Role.java                     # Enum: STUDENT, ADMIN
│   └── FonteVaga.java               # Enum: JOOBLE, JSOUP, ADMIN
├── repository/
│   ├── UsuarioRepository.java        # CRUD + findByEmail, findByTokenRecuperacao
│   ├── VagaRepository.java           # CRUD + buscarComFiltros (JPQL), existsByUrl
│   └── VagaSelecionadaRepository.java# Relacao usuario-vaga selecionada
├── security/
│   ├── JwtUtil.java                  # Gerar, validar e extrair dados de tokens JWT
│   └── JwtAuthFilter.java           # Filtro que intercepta requests e seta autenticacao
├── service/
│   ├── AuthService.java              # Registro, login, recuperar/alterar senha
│   ├── UsuarioService.java           # Buscar/atualizar perfil do estudante
│   ├── VagaService.java              # CRUD de vagas, filtros, conversao DTO
│   ├── VagaSelecionadaService.java   # Selecionar/remover vagas do estudante
│   ├── AdminService.java             # Operacoes administrativas (delega para outros services)
│   ├── JoobleService.java            # Importacao de vagas da API Jooble
│   └── ScraperService.java           # Web scraping de vagas com JSoup
└── exception/
    ├── GlobalExceptionHandler.java   # Tratamento centralizado de erros
    └── ResourceNotFoundException.java# Exception para 404
```

## Fluxo de Autenticacao

```
1. Estudante faz POST /api/auth/register com nome, email, senha
2. Sistema cria usuario com role STUDENT e retorna UsuarioDTO
3. Estudante faz POST /api/auth/login com email e senha
4. Sistema valida credenciais, gera token JWT e retorna LoginResponse
5. Estudante envia token no header "Authorization: Bearer <token>" nas proximas requests
6. JwtAuthFilter intercepta a request, valida o token, e seta o SecurityContext
7. Spring Security verifica se o usuario tem permissao para acessar o endpoint
```

## Mapa de Endpoints

### Publicos (sem token)
| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | `/api/auth/register` | Registrar novo estudante |
| POST | `/api/auth/login` | Login (retorna JWT) |
| POST | `/api/auth/forgot-password` | Solicitar token de recuperacao |
| PUT | `/api/auth/reset-password` | Alterar senha com token |
| GET | `/api/jobs` | Listar vagas (com filtros opcionais) |
| GET | `/api/jobs/{id}` | Buscar vaga por ID |
| GET | `/api/jobs/search-external` | Importar vagas do Jooble e retornar |

### Autenticados (token JWT)
| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/profile` | Ver dados do estudante autenticado |
| PUT | `/api/profile` | Atualizar nome, curso, periodo |
| POST | `/api/jobs/select` | Selecionar uma vaga |
| GET | `/api/jobs/my-selections` | Listar vagas selecionadas |
| DELETE | `/api/jobs/my-selections/{vagaId}` | Remover vaga da lista |

### Admin (token JWT + role ADMIN)
| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/admin/users` | Listar todos os usuarios |
| GET | `/api/admin/users/{id}` | Buscar usuario por ID |
| PUT | `/api/admin/users/{id}` | Atualizar usuario |
| GET | `/api/admin/jobs` | Listar todas as vagas |
| POST | `/api/admin/jobs` | Criar vaga manualmente |
| PUT | `/api/admin/jobs/{id}` | Atualizar vaga |
| DELETE | `/api/admin/jobs/{id}` | Deletar vaga |
| POST | `/api/admin/import-scraper` | Executar scraping manual |
| POST | `/api/admin/import-jooble` | Importar vagas do Jooble |

## Fontes de Vagas

O sistema agrega vagas de 3 fontes:

1. **JOOBLE** — API externa. O `JoobleService` faz POST na API Jooble com keywords e localizacao, parseia o JSON e salva no banco.
2. **JSOUP** — Web scraping. O `ScraperService` acessa uma URL configurada, extrai dados com seletores CSS e salva no banco. Pode rodar via cron (`scraper.cron`) ou manualmente pelo admin.
3. **ADMIN** — Cadastro manual pelo painel admin.

## Fluxo do Estudante

```
1. Registra-se na plataforma
2. Faz login e recebe token JWT
3. Navega pelas vagas (GET /api/jobs com filtros de busca, localizacao, fonte)
4. Seleciona vagas de interesse (POST /api/jobs/select)
5. Consulta suas vagas selecionadas (GET /api/jobs/my-selections)
6. Remove vagas que nao interessam mais (DELETE /api/jobs/my-selections/{id})
7. Atualiza seus dados de perfil quando necessario (PUT /api/profile)
```

## Profiles

| Profile | Banco | DDL | CORS | Uso |
|---|---|---|---|---|
| `dev` | PostgreSQL local (localhost:5432) | update | `*` | Desenvolvimento |
| `prod` | PostgreSQL via env vars | validate | Restrito | Producao |
| `test` | H2 em memoria | create-drop | `*` | Testes automatizados |

## Swagger

- **Dinamico** (app rodando): `http://localhost:8080/swagger-ui.html`
- **Estatico** (GitHub Pages): `https://pedrovictorxd.github.io/ConectaTech/`
- O arquivo `docs/swagger/openapi.json` e deployado automaticamente pelo GitHub Pages a partir da branch `develop`
