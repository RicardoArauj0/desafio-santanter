
# API de Busca de CEP

## Sobre o Projeto

Esta é uma API REST desenvolvida em Java com Spring Boot que permite aos usuários buscar informações de endereço a partir de um Código de Endereçamento Postal (CEP) brasileiro. A aplicação também registra todas as buscas realizadas em um log.

## Tecnologias Utilizadas

*   **Java 21**
*   **Spring Boot 3.x**
*   **Spring Data JPA**
*   **Maven**
*   **MySQL** (Banco de Dados)
*   **Docker**
*   **WireMock**
*   **Lombok**

## Como Executar

Existem duas maneiras de configurar e executar o projeto: usando Docker Compose (recomendado) ou configurando cada serviço manualmente.

### Com Docker Compose (Recomendado)

Este método utiliza o `docker-compose.yaml` para subir o banco de dados MySQL e o servidor WireMock para simular a API de CEP.

**Pré-requisitos:**
*   Docker e Docker Compose
*   Java 21 ou superior
*   Maven 3.6 ou superior

**Passos:**

1.  **Clone o repositório:**
    Clone o repositório no GitHub
    

2.  **Inicie os serviços com Docker:**
    Na raiz do projeto, execute o comando para iniciar o MySQL e o WireMock.
    ```bash
    docker-compose up -d
    ```
    *   O **MySQL** estará disponível em `localhost:3306`.
    *   O **WireMock** (simulador da API de CEP) estará em `localhost:8081`.

3.  **Configure a aplicação:**
    Certifique-se de que seu arquivo `src/main/resources/application.properties` está configurado para se conectar aos serviços do Docker. As credenciais para o banco de dados são `root` / `password` e o nome do banco é `cep`.

4.  **Construa e execute a aplicação:**
    ```bash
    mvn clean install
    java -jar target/santander-0.0.1-SNAPSHOT.jar
    ```
    ou usando o maven:   

   ```bash
    mvn spring-boot:run
   ``` 


    A API estará disponível em `http://localhost:8080`.

### Manualmente

Este método exige que você instale e configure o MySQL por conta própria.

**Pré-requisitos:**
*   Java 21 ou superior
*   Maven 3.6 ou superior
*   Uma instância de MySQL em execução

**Passos:**

1.  **Clone o repositório:**
    Clone o repositório no GitHub

2.  **Configure o Banco de Dados:**
    *   Crie um banco de dados no MySQL com o nome `cep`.
    *   Atualize as informações de conexão (URL, usuário e senha) no arquivo `src/main/resources/application.properties`.

3.  **Construa o projeto:**
    ```bash
    mvn clean install
    ```

4.  **Execute a aplicação:**
    ```bash
    java -jar target/santander-0.0.1-SNAPSHOT.jar
    ```

## Endpoints da API

A URL base para todos os endpoints é `/cep`.

### 1. Buscar Endereço por CEP

Este endpoint retorna as informações de endereço correspondentes a um CEP fornecido.

*   **Método:** `POST`
*   **URL:** `/cep/busca`
*   **Corpo da Requisição (JSON):**
    ```json
    {
      "cep": "01001000"
    }
    ```
*   **Exemplo com `curl`:**
    ```bash
    curl -X POST http://localhost:8080/cep/busca \
    -H "Content-Type: application/json" \
    -d '{"cep": "01001000"}'
    ```
*   **Resposta de Sucesso (200 OK):**
    ```json
    {
        "cep": "01001-000",
        "logradouro": "Praça da Sé",
        "complemento": "lado ímpar",
        "bairro": "Sé",
        "localidade": "São Paulo",
        "uf": "SP"
    }
    ```

### 2. Listar Logs de Busca

Este endpoint retorna uma lista de todas as buscas de CEP que foram realizadas.

*   **Método:** `GET`
*   **URL:** `/cep/log`
*   **Exemplo com `curl`:**
    ```bash
    curl -X GET http://localhost:8080/cep/log
    ```
*   **Resposta de Sucesso (200 OK):**
    ```json
    [
        {
            "cep": "01001-000",
            "logradouro": "Praça da Sé",
            "localidade": "São Paulo",
            "dataBusca": "2024-08-01T10:30:00Z"
        },
        {
            "cep": "20031-050",
            "logradouro": "Avenida Churchill",
            "localidade": "Rio de Janeiro",
            "dataBusca": "2024-08-01T10:32:15Z"
        }
    ]
    ```

## Possiveis melhorias

* Adicionar paginação e filtros no endpoint GET /cep/log
* Externalizar as credenciais do banco de dados em algum cerviço de externo 
  (Ex: Google Cloud Secret Manager, AWS Secret Manager 
* Resiliência na Integração Externa (Resilience4j) - Circuit breaker para evitar chamadas cintinuas 
  para uma API com erro. Retry e timeout
* Documentação Automática (Swagger/OpenAPI)
* Adicionar o Spring Boot Actuator e integrar o Micrometer.
  Isso expõe endpoints (como /actuator/health e /actuator/metrics) essenciais para verificar a saúde do contêiner
  rodando em nuvem e exportar métricas da aplicação e tempo de duração da chamada externa para ferramentas como Prometheus, Grafana e DataDog.
