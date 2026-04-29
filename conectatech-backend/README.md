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

## Configuracao de email para recuperacao de senha

O projeto agora suporta envio de email de recuperacao de senha para estudante e empresa.

Variaveis recomendadas para uso com Brevo SMTP:

```bash
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=<usuario-smtp-brevo>
MAIL_PASSWORD=<chave-smtp-brevo>
MAIL_FROM=noreply@seudominio.com
RESET_PASSWORD_URL=http://localhost:3000/reset-password
CONFIRM_EMAIL_URL=http://localhost:3000/confirm-email
```

Observacoes:

- use a chave SMTP da Brevo, nao a API key HTTP
- no cadastro, a conta nasce pendente e o login so libera apos confirmacao do email
- no fluxo atual, o frontend deve ler `token` e `tipo` da URL de reset
- no fluxo de confirmacao, o frontend deve ler `token` e `tipo` da URL recebida por email
- o token de recuperacao expira em 1 hora por padrao
- o token de confirmacao de email expira em 24 horas por padrao

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
