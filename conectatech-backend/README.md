# ConectaTech API

Backend da plataforma **ConectaTech** — conectando estudantes de diversos cursos da UNP (Universidade Potiguar) a vagas de estagio na regiao de Mossoro/RN.

## Versao Java

- **Java 17** (OpenJDK 17.0.18 ou superior)

## Como rodar

```bash
# 1. Clonar o repositorio
git clone https://github.com/PedroVictorxD/ConectaTech.git
cd conectatech-backend

# 2. Criar o banco de dados PostgreSQL
psql -U postgres -c "CREATE DATABASE conectatech;"

# 3. Rodar a aplicacao
./mvnw spring-boot:run

# Windows:
mvnw.cmd spring-boot:run
```

A API estara disponivel em `http://localhost:8080`.

## Como conectar ao GitHub

```bash
# Verificar remote atual
git remote -v

# Adicionar remote (se ainda nao tiver)
git remote add origin https://github.com/PedroVictorxD/ConectaTech.git

# Enviar alteracoes
git push -u origin <nome-da-branch>

# Baixar alteracoes
git pull origin <nome-da-branch>
```

## Licenca

Projeto academico — UNP Extensao 2026.
