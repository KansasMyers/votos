# Sistema de Votação Cooperativa (DESAFIO TÉCNICO JAVA - SICREDI)

API REST desenvolvida para gerenciar pautas, sessões de votação e contabilização de votos em assembleias de cooperativas. O sistema foi projetado com foco em alta performance, concorrência massiva e integridade de dados.<br>
<b>Todos os requisitos foram concluídos com sucesso, exceto o Bônus 1, pois o link fornecido estava indisponível. Fui orientado pela equipe de contratação a prosseguir sem a realização dessa etapa.</b>

## Diferenciais Técnicos
- <b>Java 21 & Virtual Threads (Project Loom):</b> O sistema utiliza threads leves para processar milhares de votos simultâneos com baixíssimo consumo de recursos
- <b>Apache Kafka (Kraft Mode):</b> Mensageria assíncrona para notificar o resultado das pautas, garantindo desacoplamento e resiliência
- <b>PostgreSQL com Índices Estratégicos:</b> Garantia de unicidade de votos por associado/pauta e alta velocidade na contabilização
- <b>Spring Boot 4 + Testcontainers:</b> Suíte de testes automatizados que utiliza containers reais para validar a integração com o banco de dados e o broker de mensagens

## Tecnologias Utilizadas
- <b>Linguagem:</b> Java 21
- <b>Framework:</b> Spring Boot 4
- <b>Banco de Dados:</b> PostgreSQL 16
- <b>Mensageria:</b> Apache Kafka (com suporte a Kraft)
- <b>Migrações:</b> Flyway
- <b>Documentação:</b> Swagger/OpenAPI (v3)
- <b>Testes:</b> JUnit 5, Mockito, Testcontainers

## Como Executar o Projeto
### Pré-requisitos

- Docker e Docker Compose instalados
- Java 21

### Passo a passo
1. Na raiz do projeto, execute o comando para subir o banco de dados e o Kafka:

```shell
docker-compose up -d
```
2. Executar a Aplicação

```shell
# Linux
./mvnw spring-boot:run
# Windows
./mvnw.cmd spring-boot:run
```
3. Acessar a Documentação

Com a aplicação rodando, acesse o Swagger para testar os endpoints:  [Swagger](http://localhost:8080/swagger-ui.html)

## Suíte de Testes
O projeto possui uma pirâmide de testes completa:
1. <b>Testes Unitários:</b> Validam regras de negócio isoladas (ex: impedir voto em sessão fechada)
2. <b>Testes de Integração:</b> Validam o fluxo completo usando containers reais de Postgres e Kafka
3. <b>Testes de Estresse:</b> Simulam 1.000+ votos simultâneos para validar o throughput com Virtual Threads

### Executar testes
```shell
# Linux
./mvnw test
# Windows
./mvnw.cmd test
```

## Decisões de Arquitetura

1. Versionamento da API

Utilizado versionamento via URL (/v1/...) para garantir a evolução da API sem quebrar clientes legados

2. Idempotência e Concorrência

Para evitar que um associado vote duas vezes na mesma pauta (mesmo em ataques de concorrência), implementamos uma Constraint Unique no banco de dados *(associado_id + pauta_id)*, além da validação em nível de serviço

3. Fechamento de Sessão Automático

Implementado um Scheduled Task que verifica sessões expiradas a cada 10 segundos, contabiliza os votos e posta o resultado automaticamente no tópico do Kafka, garantindo que o requisito de notificação seja cumprido sem intervenção humana

4. Performance

A ativação das Virtual Threads via configuração:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```