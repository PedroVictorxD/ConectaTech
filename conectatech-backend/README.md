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
