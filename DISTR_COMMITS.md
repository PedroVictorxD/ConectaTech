# Plano de Distribuição de Commits (Git Flow)

Este documento descreve como organizar os últimos commits realizados na branch `feature/scraping-e-busca-vagas` para distribuí-los corretamente para suas respectivas branches de feature seguindo o padrão Git Flow, utilizando o comando `git cherry-pick`.

A branch atual `feature/scraping-e-busca-vagas` possui diversas alterações de diferentes contextos que foram desenvolvidas em conjunto. Para manter o histórico limpo, vamos separá-las em novas branches derivadas da `develop`.

---

## 📋 Lista de Commits Recentes a Distribuir

Os commits que precisamos organizar:
- `33b0f68` fix: allow unauthenticated GET on /api/vagas/{id}
- `fa6719d` chore: support env variable for jooble api key
- `8fb5a8b` feat: add jooble import endpoint and public external search
- `71a38b2` feat: rewrite scraper with configurable target and better logging
- `9187ee7` feat: add search filters by location and source to vagas endpoint
- `7bad645` fix: add api key validation and timeout to jooble service
- `8df3cab` fix: address code review issues (POST for scraper, null safety, request timeout, disable SPA target)
- `7ffdf59` fix: use wildcard pattern for public GET /api/vagas/** in SecurityConfig
- `66e0648` fix: resolve JPQL null parameter type issue and configure jooble api key
- `0883996` feat: search across titulo, empresa and descricao fields

---

## 🚀 Como fazer a distribuição (Passo a Passo)

Sempre partiremos da branch `develop` (mais recente) para criar uma nova branch de feature, "pescar" os commits específicos usando `cherry-pick`, e depois mergear de novo na `develop`.

### 1. Atualizações e Correções de Vagas (Busca, Filtros, Acesso Deslogado)
Esta nova branch concentrará todas as evoluções no Controller e Service de Vagas.

```bash
git checkout develop
git pull origin develop
git checkout -b feature/vagas-search-improvements

# Aplicando os commits (na ordem cronológica mais antiga para a mais nova)
git cherry-pick 33b0f68 # allow unauthenticated GET
git cherry-pick 7ffdf59 # wildcard pattern /api/vagas/**
git cherry-pick 9187ee7 # search filters location/source
git cherry-pick 0883996 # search across titulo/empresa/descricao
git cherry-pick 66e0648 # resolve JPQL null parameter type

# Enviar e criar Pull Request
git push -u origin feature/vagas-search-improvements
```

### 2. Melhorias na Integração do Jooble
Concentrará as novas configurações, timeout, e endpoints relacionados ao parceiro Jooble.

```bash
git checkout develop
git checkout -b feature/jooble-improvements

# Aplicando os commits
git cherry-pick fa6719d # support env variable
git cherry-pick 8fb5a8b # import endpoint
git cherry-pick 7bad645 # validation and timeout

# Enviar e criar Pull Request
git push -u origin feature/jooble-improvements
```

### 3. Melhorias no Web Scraper (JSoup e CIEE)
Concentrará os ajustes no scraper de vagas de estágio.

```bash
git checkout develop
git checkout -b feature/scraper-improvements

# Aplicando os commits
git cherry-pick 71a38b2 # rewrite scraper configuring target
git cherry-pick 8df3cab # address code review issues for scraper

# Enviar e criar Pull Request
git push -u origin feature/scraper-improvements
```

---

## 🗑️ Como finalizar após a distribuição

Após você realizar os comandos acima, os commits estarão separados por responsabilidade e prontos para revisar e mergear em `develop`.

1. Acesse o GitHub.
2. Crie Pull Requests das 3 novas branches em direção à branch `develop`.
3. Aceite e faça o merge.
4. (Opcional) A branch genérica `feature/scraping-e-busca-vagas` que acumulou tudo temporariamente **pode ser excluída**, pois as mudanças agora estarão salvas no Git Flow corretamente:
```bash
git branch -D feature/scraping-e-busca-vagas
```
