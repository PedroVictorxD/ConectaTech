# ConectaTech API

Backend da plataforma **ConectaTech** — interligando vagas de estágio com estudantes da UNP (Universidade Potiguar), Mossoró/RN.

## Stack

- **Java 17** + **Spring Boot 3.2.5**
- **PostgreSQL** (banco de dados)
- **Spring Security** + **JWT** (autenticação)
- **JSoup** (web scraping)
- **Jooble API** (vagas externas)
- **Maven** (build)
- **Lombok** (produtividade)

## Pré-requisitos

- Java 17+
- PostgreSQL rodando na porta 5432
- Banco `conectatech` criado

## Como rodar

```bash
# Clonar
git clone <url-do-repo>
cd conectatech-backend

# Rodar
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

## Configuracao

### Desenvolvimento (padrao)

Basta rodar `./mvnw spring-boot:run`. O profile `dev` eh ativado automaticamente com valores locais.

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

## Estrutura Git Flow

| Branch | Função |
|--------|--------|
| `main` | Versão estável |
| `develop` | Desenvolvimento contínuo |
| `feature/*` | Features individuais |
| `release/*` | Preparação de release |
| `hotfix/*` | Correções urgentes |

## Licença

Projeto acadêmico — UNP Extensão 2026.
