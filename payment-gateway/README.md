# 📦 Payment Gateway - Projeto Backend

Gateway de pagamento simplificado desenvolvido em Java com Spring Boot, oferecendo uma API REST completa para gerenciamento de usuários, contas, cobranças e transações financeiras.

## 🎯 Sobre o Projeto

Este é um sistema backend que simula um gateway de pagamento, permitindo:

- Cadastro e autenticação de usuários
- Gerenciamento de saldo em conta
- Criação e gestão de cobranças entre usuários
- Processamento de pagamentos via saldo ou cartão de crédito
- Integração com autorizadores externos
- Auditoria completa de transações

## 🏗️ Tecnologias Utilizadas

### Core
- **Java 17** - Linguagem de programação
- **Spring Boot 3.4.1** - Framework principal
- **Maven** - Gerenciamento de dependências e build

### Banco de Dados
- **PostgreSQL 16** - Banco de dados relacional
- **Flyway** - Controle de migrations
- **Spring Data JPA** - Camada de persistência
- **Hibernate** - ORM

### Segurança
- **Spring Security** - Framework de segurança
- **JWT (JSON Web Tokens)** - Autenticação e autorização
- **BCrypt** - Criptografia de senhas

### Documentação
- **SpringDoc OpenAPI** - Geração automática de documentação
- **Swagger UI** - Interface interativa para testes da API

### Integrações
- **Spring Cloud OpenFeign** - Cliente HTTP declarativo para integração com APIs externas
- **Resilience4j** - Circuit breaker e retry para resiliência

### Testes e Qualidade
- **JUnit 5** - Framework de testes
- **Mockito** - Mocks para testes unitários
- **Spring Boot Test** - Testes de integração
- **JaCoCo** - Cobertura de código
- **SonarQube** - Análise estática de código

### Containerização
- **Docker** - Containerização da aplicação
- **Docker Compose** - Orquestração de containers

## 📂 Estrutura do Projeto

```
payment-gateway/
├── src/
│   ├── main/
│   │   ├── java/br/com/nimblebaas/payment_gateway/
│   │   │   ├── clients/           # Clientes HTTP (Feign) para APIs externas
│   │   │   │   ├── authorizer/   # Cliente do autorizador de pagamentos
│   │   │   │   └── configuration/ # Configurações do Feign
│   │   │   ├── configs/          # Configurações gerais da aplicação
│   │   │   │   ├── authentication/ # Config de JWT
│   │   │   │   ├── security/     # Config do Spring Security
│   │   │   │   └── OpenApiConfig # Config do Swagger
│   │   │   ├── controllers/      # Controllers REST
│   │   │   │   ├── account/      # Endpoints de conta
│   │   │   │   ├── authentication/ # Endpoints de autenticação
│   │   │   │   ├── charge/       # Endpoints de cobranças
│   │   │   │   └── users/        # Endpoints de usuários
│   │   │   ├── dtos/             # Data Transfer Objects
│   │   │   │   ├── input/        # DTOs de entrada (requests)
│   │   │   │   ├── output/       # DTOs de saída (responses)
│   │   │   │   └── internal/     # DTOs internos
│   │   │   ├── entities/         # Entidades JPA
│   │   │   ├── enums/            # Enumerações
│   │   │   ├── events/           # Eventos da aplicação
│   │   │   ├── exceptions/       # Exceções customizadas
│   │   │   ├── filters/          # Filtros HTTP (autenticação)
│   │   │   ├── helpers/          # Classes auxiliares e utilitárias
│   │   │   ├── listeners/        # Listeners de eventos
│   │   │   ├── repositories/     # Repositórios JPA
│   │   │   └── services/         # Lógica de negócio
│   │   └── resources/
│   │       ├── application.properties # Configurações da aplicação
│   │       └── db/migration/     # Scripts Flyway
│   └── test/                     # Testes unitários e de integração
├── pom.xml                       # Configuração Maven
├── Dockerfile                    # Dockerfile para build da imagem
└── SWAGGER.md                    # Documentação do Swagger

```

## 🔑 Principais Funcionalidades

### 1. Gerenciamento de Usuários

#### Cadastro
- Validação de CPF (formato e dígitos verificadores)
- Validação de e-mail
- Senha forte com criptografia BCrypt
- Criação automática de conta com saldo zero

#### Autenticação
- Login via CPF ou e-mail + senha
- Geração de Access Token (JWT) com expiração de 15 minutos
- Geração de Refresh Token com expiração de 1 hora
- Renovação de tokens sem necessidade de novo login
- Auditoria de tentativas de login (sucesso e falha)

#### Gestão de Perfil
- Consulta de dados do usuário autenticado
- Busca de usuário por CPF ou e-mail
- Alteração de senha com validação da senha atual

### 2. Gestão de Contas

#### Saldo
- Consulta de saldo disponível
- Histórico de transações
- Bloqueio temporário de saldo durante transações

#### Depósitos
- Depósito de valores na conta
- Validação via autorizador externo
- Registro de transação de depósito

### 3. Sistema de Cobranças

#### Criação
- Usuário cria cobrança informando CPF do destinatário
- Valor e descrição opcional
- Validações de usuário existente e valores positivos

#### Consulta
- Listagem de cobranças enviadas (criadas pelo usuário)
- Listagem de cobranças recebidas (destinadas ao usuário)
- Filtros por status:
  - **PENDING** - Aguardando pagamento
  - **PAID** - Paga com sucesso
  - **CANCELLED** - Cancelada

#### Pagamento
- **Via Saldo**:
  - Verifica saldo suficiente
  - Bloqueia saldo temporariamente
  - Transfere valor do pagador para o destinatário
  - Registra transação
  
- **Via Cartão de Crédito**:
  - Valida dados do cartão
  - Consulta autorizador externo
  - Processa apenas se autorizado
  - Suporte a parcelamento
  - Credita valor ao destinatário

#### Cancelamento
- **Cobrança Pendente**: Apenas altera status
- **Cobrança Paga via Saldo**: Estorna valor ao pagador
- **Cobrança Paga via Cartão**: Consulta autorizador e cancela se aprovado

### 4. Integrações Externas

#### Autorizador de Pagamentos
- URL configurável via variável de ambiente
- Validação de depósitos
- Autorização de pagamentos com cartão
- Cancelamento de transações
- Retry automático em caso de falha temporária
- Circuit breaker para proteção

## 🔐 Segurança

### Autenticação e Autorização
- JWT com chave secreta configurável
- Tokens de curta duração (15 min)
- Refresh tokens para renovação
- Endpoints públicos e protegidos

### Proteção de Dados
- Senhas sempre criptografadas (BCrypt)
- Validação de CPF para evitar dados inválidos
- Sanitização de inputs
- Auditoria de acessos

### Endpoints Públicos
- `POST /users/register` - Registro de usuário
- `POST /authentication/login` - Login
- `POST /authentication/refresh-token` - Renovação de token
- `GET /swagger-ui.html` - Documentação
- `GET /api-docs` - Especificação OpenAPI

### Endpoints Protegidos
Todos os demais endpoints requerem autenticação via Bearer Token no header:
```
Authorization: Bearer {accessToken}
```

## 📊 Modelo de Dados

### Principais Entidades

- **User**: Dados cadastrais do usuário (nome, CPF, e-mail, senha)
- **Account**: Conta bancária do usuário (saldo disponível)
- **Charge**: Cobrança entre usuários
- **Transaction**: Transações financeiras (depósito, pagamento, estorno)
- **ChargePayment**: Relação entre cobrança e transação de pagamento
- **HoldBalance**: Bloqueio temporário de saldo
- **RefreshToken**: Tokens de renovação de acesso
- **AuthenticationAudit**: Auditoria de tentativas de login

### Relacionamentos

```
User 1---1 Account
User 1---N Charge (como criador)
User 1---N Charge (como destinatário)
Charge 1---N ChargePayment
Transaction N---1 ChargePayment
Account 1---N Transaction
Account 1---N HoldBalance
```

## 🚀 Executando o Projeto

### Pré-requisitos
- Java 17+
- Maven 3.8+
- Docker e Docker Compose (para ambiente completo)

### Com Docker (Recomendado)

Consulte o [Guia de Setup](../SETUP.md) para instruções detalhadas.

```bash
# Na raiz do projeto
./run.sh
```

### Localmente (Desenvolvimento)

```bash
# 1. Configure as variáveis de ambiente
export DB_HOST=localhost
export DB_PORT=5432
export DB_USER=seu_usuario
export DB_PASSWORD=sua_senha
export DB_APP_DB=payment_gateway_db
export PAYMENT_GATEWAY_PORT=8080
export APP_SECURITY_JWT_SECRET=sua_chave_secreta_jwt
export APP_API_AUTHORIZER_URL=https://util.devi.tools/api/v2/authorize

# 2. Compile o projeto
mvn clean install

# 3. Execute
mvn spring-boot:run

# Ou execute o JAR
java -jar target/payment-gateway-1.0.0.jar
```

## 🧪 Testes

### Executar todos os testes
```bash
mvn test
```

### Executar com cobertura
```bash
mvn clean test jacoco:report
```

O relatório será gerado em: `target/site/jacoco/index.html`

### Testes Disponíveis
- **Testes Unitários**: Services, Helpers, Validators
- **Testes de Integração**: Controllers, Repositories
- **Testes de Cliente HTTP**: Clientes Feign com WireMock

## 📖 Documentação da API

A API possui documentação interativa via Swagger.

Após iniciar a aplicação, acesse:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

Consulte [SWAGGER.md](./SWAGGER.md) para guia detalhado de uso.

## 🔨 Build e Deploy

### Build da imagem Docker

```bash
cd payment-gateway
docker build -t payment-gateway:latest .
```

### Deploy com Docker Compose

```bash
# Na raiz do projeto
docker compose up -d
```

## 🗃️ Migrations

As migrations são gerenciadas pelo Flyway e executadas automaticamente no start da aplicação.

### Criar nova migration

```bash
# Linux/Mac
./create_migration.sh "nome_da_migration"

# Windows (PowerShell)
./create_migration.ps1 "nome_da_migration"

# Windows (CMD)
./create_migration.bat "nome_da_migration"
```

### Verificar status das migrations

```bash
mvn flyway:info
```

## 🛠️ Desenvolvimento

### Convenções de Código

O projeto segue os princípios de **Clean Code**:
- Nomes descritivos e autoexplicativos
- Métodos pequenos e com responsabilidade única
- Classes coesas com baixo acoplamento
- Comentários apenas quando necessário
- Tratamento adequado de exceções
- Validações em todas as entradas

### Padrões Utilizados

- **DTOs**: Separação entre camadas (input/output/internal)
- **Repository Pattern**: Acesso a dados
- **Service Layer**: Lógica de negócio
- **Events**: Desacoplamento de funcionalidades (ex: auditoria)
- **Exception Handling**: Tratamento global via @ControllerAdvice
- **Builder Pattern**: Construção de objetos complexos

### Estrutura de Pacotes

- `clients`: Integrações com APIs externas
- `configs`: Configurações da aplicação
- `controllers`: Camada de apresentação (REST)
- `dtos`: Objetos de transferência de dados
- `entities`: Entidades do domínio (JPA)
- `enums`: Enumerações
- `events`: Eventos da aplicação
- `exceptions`: Exceções customizadas
- `filters`: Filtros HTTP
- `helpers`: Utilitários e validadores
- `listeners`: Ouvintes de eventos
- `repositories`: Acesso a dados
- `services`: Lógica de negócio

## 📈 Análise de Código

### SonarQube

O projeto está integrado com SonarQube para análise de qualidade.

```bash
# Após executar ./run.sh, acesse:
http://localhost:9000

# Para executar análise manualmente:
cd payment-gateway
mvn clean verify sonar:sonar
```

### Métricas Monitoradas
- Cobertura de código
- Code smells
- Bugs
- Vulnerabilidades de segurança
- Duplicação de código
- Complexidade ciclomática

## 🐛 Troubleshooting

### Porta já em uso
```bash
# Altere a porta no arquivo .env
PAYMENT_GATEWAY_PORT=8081
```

### Erro de conexão com banco
- Verifique se o PostgreSQL está rodando
- Confirme as credenciais no `.env`
- Verifique se as migrations foram executadas

### JWT inválido
- Verifique se a chave JWT está configurada
- Verifique se o token não expirou
- Use o endpoint `/authentication/refresh-token` para renovar

## 📞 Endpoints Principais

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| POST | `/users/register` | Registrar usuário | Não |
| POST | `/authentication/login` | Login | Não |
| POST | `/authentication/refresh-token` | Renovar token | Não |
| GET | `/users/me` | Dados do usuário | Sim |
| GET | `/accounts/balance` | Consultar saldo | Sim |
| POST | `/accounts/deposit` | Depositar | Sim |
| POST | `/charges` | Criar cobrança | Sim |
| GET | `/charges/sent` | Cobranças enviadas | Sim |
| GET | `/charges/received` | Cobranças recebidas | Sim |
| POST | `/charges/pay` | Pagar cobrança | Sim |
| POST | `/charges/cancel` | Cancelar cobrança | Sim |

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Siga as convenções de código
4. Escreva testes para novas funcionalidades
5. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
6. Push para a branch (`git push origin feature/MinhaFeature`)
7. Abra um Pull Request

## 📄 Licença

Este projeto é desenvolvido para fins educacionais e demonstrativos.

---

**Desenvolvido com ☕ e Spring Boot**

