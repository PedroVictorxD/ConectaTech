# ConectaTech API

Backend da plataforma **ConectaTech** — conectando estudantes de diversos cursos da UNP (Universidade Potiguar) a vagas de estagio na regiao de Mossoro/RN.

## Stack

- **Java 17** + **Spring Boot 3.2.5**
- **PostgreSQL** (banco de dados)
- **Spring Security** + **JWT** (autenticacao)
- **SpringDoc OpenAPI** (documentacao Swagger)
- **JSoup** (web scraping)
- **Jooble API** (vagas externas)
- **Maven** (build)
- **Lombok** (produtividade)

## Pre-requisitos

- **Java 17** (OpenJDK 17.0.18 ou superior)
- **PostgreSQL 14+** rodando na porta 5432
- Banco de dados `conectatech` criado

## Como rodar

```bash
# 1. Clonar o repositorio
git clone https://github.com/PedroVictorxD/ConectaTech.git
cd conectatech-backend

# 2. Criar o banco de dados (se ainda nao existir)
psql -U postgres -c "CREATE DATABASE conectatech;"

# 3. Verificar versao do Java
java -version   # deve ser 17+

# 4. Rodar a aplicacao (Maven Wrapper incluso, nao precisa instalar Maven)
./mvnw spring-boot:run

# Ou no Windows:
mvnw.cmd spring-boot:run
```

A API estara disponivel em `http://localhost:8080`.

O profile `dev` e ativado automaticamente. As tabelas sao criadas/atualizadas pelo Hibernate (`ddl-auto=update`).

## Documentacao da API (Swagger)

Com a aplicacao rodando, acesse:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

A documentacao inclui todos os endpoints com descricoes, exemplos de request/response e autenticacao JWT integrada.

## Endpoints Principais

### Auth (publico)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/api/auth/register` | Registrar novo estudante |
| POST | `/api/auth/login` | Login (retorna token JWT) |
| POST | `/api/auth/recuperar-senha` | Solicitar recuperacao de senha |
| PUT | `/api/auth/alterar-senha` | Alterar senha com token |

### Vagas (publico para leitura)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/api/vagas` | Listar vagas com filtros (busca, localizacao, fonte) |
| GET | `/api/vagas/{id}` | Buscar vaga por ID |
| GET | `/api/vagas/buscar-externas` | Importar e buscar vagas do Jooble |
| POST | `/api/vagas/selecionar` | Selecionar/favoritar vaga (auth) |
| GET | `/api/vagas/minhas` | Listar vagas selecionadas (auth) |
| DELETE | `/api/vagas/minhas/{vagaId}` | Remover vaga selecionada (auth) |

### Usuario (autenticado)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/api/meus-dados` | Ver dados do perfil |
| PUT | `/api/meus-dados` | Atualizar perfil |

### Admin (role ADMIN)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/api/admin/usuarios` | Listar usuarios |
| GET | `/api/admin/usuarios/{id}` | Buscar usuario |
| PUT | `/api/admin/usuarios/{id}` | Atualizar usuario |
| POST | `/api/admin/importar-scraper` | Executar scraping manual |
| POST | `/api/admin/importar-jooble` | Importar vagas do Jooble |
| GET | `/api/admin/vagas` | Listar todas as vagas |
| POST | `/api/admin/vagas` | Criar vaga manual |
| PUT | `/api/admin/vagas/{id}` | Atualizar vaga |
| DELETE | `/api/admin/vagas/{id}` | Deletar vaga |

## Configuracao

### Desenvolvimento (padrao)

Basta rodar `./mvnw spring-boot:run`. O profile `dev` e ativado automaticamente com valores locais.

### Producao

Defina as variaveis de ambiente antes de iniciar:

| Variavel | Descricao | Obrigatoria |
|----------|-----------|-------------|
| `SPRING_PROFILE` | Perfil ativo (`prod`) | Sim |
| `DATABASE_URL` | URL JDBC do PostgreSQL | Sim |
| `DATABASE_USER` | Usuario do banco | Sim |
| `DATABASE_PASSWORD` | Senha do banco | Sim |
| `JWT_SECRET` | Chave secreta para tokens JWT (min 32 chars) | Sim |
| `JOOBLE_API_KEY` | Chave da API Jooble | Sim |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas (separadas por virgula) | Nao (default: http://localhost:3000) |

```bash
export SPRING_PROFILE=prod
export DATABASE_URL=jdbc:postgresql://host:5432/conectatech
export DATABASE_USER=usuario
export DATABASE_PASSWORD=senha_segura
export JWT_SECRET=sua_chave_secreta_com_pelo_menos_32_caracteres
export JOOBLE_API_KEY=sua_chave_jooble
./mvnw spring-boot:run
```

## Estrutura do Projeto

```
src/main/java/br/com/unp/conectatech/
├── config/          # SecurityConfig, OpenApiConfig
├── controller/      # AuthController, VagaController, AdminController, UsuarioController
├── dto/             # Request/Response DTOs
├── exception/       # GlobalExceptionHandler, ResourceNotFoundException
├── model/           # Entidades JPA (Usuario, Vaga, VagaSelecionada, Role, FonteVaga)
├── repository/      # Spring Data JPA repositories
├── security/        # JwtUtil, JwtAuthFilter
└── service/         # AuthService, VagaService, AdminService, ScraperService, JoobleService
```

## Git Flow

| Branch | Funcao |
|--------|--------|
| `main` | Versao estavel |
| `develop` | Desenvolvimento continuo |
| `feature/*` | Features individuais |

## Licenca

Projeto academico — UNP Extensao 2026.
