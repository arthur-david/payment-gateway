# 🚀 Guia de Setup e Execução

Este guia explica como configurar e executar o Payment Gateway usando Docker e Docker Compose.

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Docker** (versão 20.10 ou superior)
- **Docker Compose** (versão 2.0 ou superior)
- **Git** (para clonar o repositório)

### Verificar instalação

```bash
docker --version
docker compose version
```

## ⚙️ Configuração das Variáveis de Ambiente

O projeto utiliza variáveis de ambiente para configuração. Você precisa criar um arquivo `.env` na raiz do projeto.

### 1. Criar o arquivo .env

Crie um arquivo chamado `.env` na raiz do projeto com as seguintes variáveis:

```bash
# Configurações do Banco de Dados
DB_HOST=postgres_db
DB_PORT=5432
DB_USER=payment_gateway_user
DB_PASSWORD=sua_senha_super_secreta_aqui
DB_APP_DB=payment_gateway_db

# Configurações do Payment Gateway
PAYMENT_GATEWAY_PORT=8080

# Configurações de Segurança (JWT)
APP_SECURITY_JWT_SECRET=sua_chave_secreta_jwt_muito_longa_e_segura_aqui_com_pelo_menos_256_bits

# URL do Autorizador Externo
APP_API_AUTHORIZER_URL=https://util.devi.tools/api/v2/authorize

# Configurações do SonarQube (opcional)
SONAR_CHANGE_PASSWORD=true
SONAR_CREATE_PASSWORD=true
```

### 2. Descrição das Variáveis

#### Banco de Dados
| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `DB_HOST` | Host do PostgreSQL (use `postgres_db` para Docker) | `postgres_db` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_USER` | Usuário do banco de dados | `payment_gateway_user` |
| `DB_PASSWORD` | Senha do banco de dados | `senhaSegura123!` |
| `DB_APP_DB` | Nome do banco de dados da aplicação | `payment_gateway_db` |

#### Payment Gateway
| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `PAYMENT_GATEWAY_PORT` | Porta em que a API será exposta | `8080` |

#### Segurança
| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `APP_SECURITY_JWT_SECRET` | Chave secreta para assinatura dos tokens JWT (mínimo 256 bits) | `MinhaChaveSecretaMuitoLongaESegura...` |

#### Integrações
| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `APP_API_AUTHORIZER_URL` | URL da API autorizadora externa | `https://util.devi.tools/api/v2/authorize` |

#### SonarQube (Opcional)
| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `SONAR_CHANGE_PASSWORD` | Alterar senha padrão do SonarQube | `true` |
| `SONAR_CREATE_PASSWORD` | Criar senha para o projeto no SonarQube | `true` |

### 3. Exemplo de .env completo

```bash
# Banco de Dados
DB_HOST=postgres_db
DB_PORT=5432
DB_USER=payment_gateway_user
DB_PASSWORD=P@ssw0rd_Secure_2024!
DB_APP_DB=payment_gateway_db

# Payment Gateway
PAYMENT_GATEWAY_PORT=8080

# Segurança
APP_SECURITY_JWT_SECRET=8f3a4b7c9d2e6f1a5b8c3d9e2f4a7b1c8d3e9f2a5b7c1d8e3f9a2b5c7d1e8f3a9b2c5d7e1f8a3b9c2d5e7f1a8b3c9d2e5f7a1b8c3d9e2f5a7b1c8d3e9f2a5b

# Integrações
APP_API_AUTHORIZER_URL=https://util.devi.tools/api/v2/authorize

# SonarQube
SONAR_CHANGE_PASSWORD=true
SONAR_CREATE_PASSWORD=true
```

> **⚠️ IMPORTANTE**: 
> - Nunca comite o arquivo `.env` no repositório
> - Use senhas fortes e únicas em produção
> - A chave JWT deve ter pelo menos 256 bits (64 caracteres hexadecimais)

## 🐳 Executando com Docker Compose

### Opção 1: Execução Automática (Recomendado)

Use o script `run.sh` que automatiza todo o processo:

```bash
chmod +x run.sh
./run.sh
```

Este script irá:
1. Subir os containers (PostgreSQL, Payment Gateway, SonarQube)
2. Aguardar inicialização (60 segundos)
3. Configurar o SonarQube e gerar token do projeto
4. Executar análise de cobertura de código

### Opção 2: Execução Manual

Se preferir executar manualmente:

```bash
# 1. Subir os containers
docker compose up -d

# 2. Verificar status dos containers
docker compose ps

# 3. Ver logs da aplicação
docker compose logs -f payment-gateway

# 4. Ver logs do banco de dados
docker compose logs -f postgres_db
```

### Comandos Úteis

```bash
# Parar todos os containers
docker compose down

# Parar e remover volumes (apaga dados do banco)
docker compose down -v

# Reconstruir imagens
docker compose build

# Subir apenas um serviço específico
docker compose up -d postgres_db

# Ver logs de todos os serviços
docker compose logs -f

# Acessar shell do container
docker compose exec payment-gateway bash
docker compose exec postgres_db psql -U payment_gateway_user -d payment_gateway_db
```

## 🔍 Verificando a Instalação

Após a execução, verifique se os serviços estão rodando:

### 1. API Payment Gateway
```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada:
```json
{
  "status": "UP"
}
```

### 2. Swagger UI
Acesse no navegador: http://localhost:8080/swagger-ui.html

### 3. PostgreSQL
```bash
docker compose exec postgres_db psql -U payment_gateway_user -d payment_gateway_db -c "\dt"
```

Deve listar as tabelas criadas pelas migrations.

### 4. SonarQube
Acesse no navegador: http://localhost:9000

Credenciais padrão:
- **Usuário**: `admin`
- **Senha**: `admin` (será solicitado para alterar no primeiro acesso)

## 🏗️ Estrutura dos Containers

O projeto utiliza três containers principais:

```
┌─────────────────────────────────────────┐
│  payment-gateway (porta 8080)           │
│  - Spring Boot Application              │
│  - API REST                             │
└──────────────┬──────────────────────────┘
               │
               ↓ depende de
┌──────────────┴──────────────────────────┐
│  postgres_db (porta 5433→5432)          │
│  - PostgreSQL 16                        │
│  - Banco: payment_gateway_db            │
│  - Banco: sonarqube                     │
└──────────────┬──────────────────────────┘
               │
               ↑ usa
┌──────────────┴──────────────────────────┐
│  sonarqube (porta 9000)                 │
│  - Análise de código                    │
│  - Cobertura de testes                  │
└─────────────────────────────────────────┘
```

## 📊 Migrations do Banco de Dados

As migrations são executadas automaticamente pelo Flyway quando a aplicação inicia.

Localização: `payment-gateway/src/main/resources/db/migration/`

Migrations disponíveis:
- `V1761429389__create_table_users.sql`
- `V1761431557__create_table_accounts.sql`
- `V1761431628__create_table_refresh_tokens.sql`
- `V1761431750__create_table_authentication_audits.sql`
- `V1761533821__create_table_hold_balances.sql`
- `V1761533972__create_table_charges.sql`
- `V1761534109__create_table_transactions.sql`
- `V1761616104__create_table_charge_payments.sql`
- E outras...

## 🧪 Testando a API

### 1. Registrar um usuário

```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "cpf": "123.456.789-00",
    "email": "joao@email.com",
    "password": "SenhaSegura123!"
  }'
```

### 2. Fazer login

```bash
curl -X POST http://localhost:8080/authentication/login \
  -H "Content-Type: application/json" \
  -d '{
    "cpfOrEmail": "joao@email.com",
    "password": "SenhaSegura123!"
  }'
```

Guarde o `accessToken` retornado.

### 3. Consultar saldo

```bash
curl -X GET http://localhost:8080/accounts/balance \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN_AQUI"
```

## 🛠️ Troubleshooting

### Container não inicia

```bash
# Ver logs detalhados
docker compose logs payment-gateway

# Verificar se a porta está em uso
sudo netstat -tulpn | grep 8080

# Reiniciar containers
docker compose restart
```

### Erro de conexão com banco de dados

```bash
# Verificar se o PostgreSQL está rodando
docker compose ps postgres_db

# Verificar logs do banco
docker compose logs postgres_db

# Testar conexão
docker compose exec postgres_db psql -U payment_gateway_user -d payment_gateway_db
```

### Migrations não executam

```bash
# Verificar status das migrations
docker compose exec postgres_db psql -U payment_gateway_user -d payment_gateway_db -c "SELECT * FROM flyway_schema_history;"

# Limpar banco e recriar (CUIDADO: apaga dados)
docker compose down -v
docker compose up -d
```

### Problemas com variáveis de ambiente

```bash
# Verificar se as variáveis foram carregadas
docker compose exec payment-gateway env | grep DB_

# Recarregar variáveis
docker compose down
docker compose up -d
```

## 🔐 Segurança em Produção

Para ambientes de produção, considere:

1. **Use secrets management** (Docker Secrets, HashiCorp Vault, AWS Secrets Manager)
2. **Não exponha portas desnecessárias** (remova mapeamento de portas do PostgreSQL)
3. **Use SSL/TLS** para comunicação
4. **Configure firewall** e network policies
5. **Atualize regularmente** as imagens Docker
6. **Monitore logs** e métricas
7. **Configure backup** do banco de dados
8. **Use senhas fortes** e rotação periódica

## 📞 Suporte

Para problemas ou dúvidas:
- Consulte os logs: `docker compose logs -f`
- Verifique a [documentação da API](./payment-gateway/SWAGGER.md)
- Revise o [README do projeto](./payment-gateway/README.md)

---

**Bom desenvolvimento! 🚀**

