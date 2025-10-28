# Documentação da API - Payment Gateway

## 📚 Visão Geral

A API do Payment Gateway está totalmente documentada usando **OpenAPI 3.0** (Swagger). A documentação interativa permite explorar todos os endpoints, ver exemplos de requisições e respostas, e até testar a API diretamente pelo navegador.

## 🚀 Como Acessar a Documentação

### 1. Inicie a Aplicação

```bash
mvn spring-boot:run
```

Ou execute o JAR compilado:

```bash
java -jar target/payment-gateway-0.0.1-SNAPSHOT.jar
```

### 2. Acesse a Interface do Swagger UI

Abra seu navegador e acesse:

**URL:** `http://localhost:8080/swagger-ui.html`

### 3. Acesse a Especificação OpenAPI (JSON)

Para integração com outras ferramentas, você pode acessar a especificação OpenAPI em formato JSON:

**URL:** `http://localhost:8080/api-docs`

## 🔐 Autenticação

A maioria dos endpoints da API requer autenticação via JWT (JSON Web Token).

### Como Autenticar no Swagger UI

1. **Faça Login:**
   - Expanda o grupo **"Autenticação"**
   - Clique no endpoint `POST /authentication/login`
   - Clique em **"Try it out"**
   - Preencha o JSON com suas credenciais:
     ```json
     {
       "cpfOrEmail": "seu.email@exemplo.com",
       "password": "SuaSenha123!"
     }
     ```
   - Clique em **"Execute"**
   - Copie o valor do campo `accessToken` da resposta

2. **Configure o Token:**
   - Clique no botão **"Authorize"** 🔒 (no topo da página)
   - Cole o token no campo de texto
   - Clique em **"Authorize"**
   - Clique em **"Close"**

3. **Pronto!** Agora você pode testar todos os endpoints autenticados.

## 📋 Estrutura da API

A API está organizada nos seguintes grupos:

### 👤 Usuários
- `POST /users/register` - Registrar novo usuário
- `GET /users` - Buscar usuário por CPF ou e-mail
- `GET /users/me` - Obter dados do usuário autenticado
- `PATCH /users/password` - Alterar senha

### 🔑 Autenticação
- `POST /authentication/login` - Realizar login
- `POST /authentication/refresh-token` - Renovar token de acesso

### 💰 Contas
- `GET /accounts/balance` - Consultar saldo
- `POST /accounts/deposit` - Realizar depósito

### 💳 Cobranças
- `POST /charges` - Criar cobrança
- `GET /charges/sent` - Listar cobranças enviadas
- `GET /charges/received` - Listar cobranças recebidas
- `POST /charges/pay` - Pagar cobrança
- `POST /charges/cancel` - Cancelar cobrança

## 🎯 Exemplos de Uso

### 1. Registrar um Novo Usuário

```json
POST /users/register
{
  "name": "João da Silva",
  "cpf": "123.456.789-00",
  "email": "joao.silva@email.com",
  "password": "SenhaSegura123!"
}
```

### 2. Criar uma Cobrança

```json
POST /charges
Authorization: Bearer {seu-token}
{
  "destinationCpf": "987.654.321-00",
  "amount": 50.00,
  "description": "Pagamento de serviço prestado"
}
```

### 3. Pagar uma Cobrança com Saldo

```json
POST /charges/pay
Authorization: Bearer {seu-token}
{
  "identifier": "550e8400-e29b-41d4-a716-446655440000",
  "paymentMethod": "BALANCE"
}
```

### 4. Pagar uma Cobrança com Cartão

```json
POST /charges/pay
Authorization: Bearer {seu-token}
{
  "identifier": "550e8400-e29b-41d4-a716-446655440000",
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "cardExpirationDate": "12/2025",
  "cardCvv": "123",
  "installments": 1
}
```

## ⚙️ Configurações

As configurações do Swagger podem ser personalizadas no arquivo `application.properties`:

```properties
# Caminho da especificação OpenAPI (JSON)
springdoc.api-docs.path=/api-docs

# Caminho da interface Swagger UI
springdoc.swagger-ui.path=/swagger-ui.html

# Ordenação das operações por método HTTP
springdoc.swagger-ui.operationsSorter=method

# Ordenação das tags (grupos) em ordem alfabética
springdoc.swagger-ui.tagsSorter=alpha

# Desabilita a URL padrão do Swagger
springdoc.swagger-ui.disable-swagger-default-url=true
```

## 🔧 Ferramentas de Terceiros

Você pode importar a especificação OpenAPI em ferramentas como:

- **Postman:** Importe via URL `http://localhost:8080/api-docs`
- **Insomnia:** Importe a especificação OpenAPI
- **VS Code REST Client:** Use a especificação para autocompletar
- **Geradores de código:** Use o OpenAPI Generator para gerar clientes em várias linguagens

## 📝 Códigos de Status HTTP

A API usa os seguintes códigos de status:

| Código | Significado |
|--------|-------------|
| 200 | OK - Requisição bem-sucedida |
| 201 | Created - Recurso criado com sucesso |
| 204 | No Content - Operação bem-sucedida sem conteúdo na resposta |
| 400 | Bad Request - Dados inválidos |
| 401 | Unauthorized - Autenticação necessária ou falhou |
| 403 | Forbidden - Sem permissão para acessar o recurso |
| 404 | Not Found - Recurso não encontrado |
| 500 | Internal Server Error - Erro interno do servidor |


