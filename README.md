# Payment Gateway

Sistema de gateway de pagamento simplificado desenvolvido em Java/Spring Boot, permitindo a criação e gerenciamento de cobranças entre usuários com opções de pagamento via saldo em conta ou cartão de crédito.

## 🎯 Visão Geral

O Payment Gateway é uma API REST completa que oferece funcionalidades essenciais para um sistema de pagamentos, incluindo:

- **Gerenciamento de Usuários**: Cadastro e autenticação de usuários via CPF/e-mail
- **Gestão de Contas**: Controle de saldo e depósitos
- **Cobranças**: Criação, consulta, pagamento e cancelamento de cobranças entre usuários
- **Múltiplos Métodos de Pagamento**: Suporte a pagamento via saldo ou cartão de crédito
- **Integração Externa**: Comunicação com autorizadores de pagamento externos
- **Auditoria**: Registro de tentativas de autenticação e transações
- **Documentação Interativa**: Swagger/OpenAPI integrado

## 🏗️ Arquitetura

O projeto utiliza:
- **Backend**: Java 17 + Spring Boot 3.4.1
- **Banco de Dados**: PostgreSQL 16
- **Migrations**: Flyway
- **Análise de Código**: SonarQube
- **Containerização**: Docker & Docker Compose
- **Documentação**: SpringDoc OpenAPI (Swagger)

## 📚 Documentação

- **[📦 Sobre o Projeto Payment Gateway](./payment-gateway/README.md)** - Estrutura detalhada do projeto, tecnologias utilizadas, arquitetura e endpoints
- **[🚀 Guia de Setup e Execução](./SETUP.md)** - Como configurar e executar o projeto com Docker, variáveis de ambiente e primeiros passos
- **[📖 API Documentation (Swagger)](./payment-gateway/SWAGGER.md)** - Guia completo de uso da documentação interativa da API

## 🎬 Início Rápido

```bash
# 1. Clone o repositório
git clone <repository-url>
cd payment-gateway

# 2. Configure as variáveis de ambiente
cp .env.example .env
# Edite o arquivo .env com suas configurações

# 3. Execute o projeto
./run.sh
```

Após a execução, acesse:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **SonarQube**: http://localhost:9000

## 🔑 Principais Funcionalidades

### Gestão de Usuários
- Cadastro com validação de CPF
- Autenticação via JWT (CPF ou e-mail + senha)
- Atualização de senha
- Consulta de dados do usuário

### Sistema de Cobranças
- Criação de cobranças entre usuários
- Consulta de cobranças enviadas e recebidas (filtradas por status)
- Pagamento via saldo ou cartão de crédito
- Cancelamento com regras de estorno automático

### Gerenciamento de Saldo
- Consulta de saldo disponível
- Depósitos com validação externa
- Bloqueio temporário de saldo durante transações
- Estorno automático em caso de falha

## 🛡️ Segurança

- Autenticação JWT com tokens de acesso e refresh
- Senhas criptografadas com BCrypt
- Auditoria de tentativas de login
- Validação de CPF e dados sensíveis
- Proteção de endpoints via Spring Security

## 🧪 Qualidade de Código

O projeto inclui:
- Testes unitários e de integração
- Cobertura de código via JaCoCo
- Análise estática com SonarQube
- Convenções de Clean Code

## 📄 Licença

Este projeto é desenvolvido para fins educacionais e demonstrativos.

## 👥 Contribuindo

Contribuições são bem-vindas! Por favor:
1. Faça fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

**Desenvolvido com ☕ e Java**

