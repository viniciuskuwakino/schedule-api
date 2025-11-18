# Schedule API

API REST em Spring Boot para gerenciamento de salas e reuniões corporativas, incluindo autenticação JWT e documentação Swagger/OpenAPI.

## Requisitos

- Java 21
- Docker / Docker Compose (para subir o PostgreSQL usado pela aplicação)

## Execução do projeto

1. **Suba toda a aplicação**
   ```bash
   docker compose up -d --build
   ```
   O serviço PostgreSQL fica disponível em `localhost:7700` com usuário `vinicius` e senha `password`.

3. **Acesse o Swagger**
   - URL: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   - Após realizar login em `/auth/login`, clique em **Authorize** e informe o token JWT para testar os endpoints protegidos.

## Endpoints principais

### Autenticação
| Método | Caminho          | Descrição                    |
|--------|------------------|------------------------------|
| POST   | `/auth/login`    | Autentica usuário e retorna JWT |
| POST   | `/auth/register` | Cria novo usuário e retorna JWT |

### Salas (`/rooms`)
| Método | Caminho                 | Descrição                                    |
|--------|-------------------------|----------------------------------------------|
| POST   | `/rooms`                | Cria uma sala                                |
| GET    | `/rooms`                | Lista salas com paginação (`page`,`size`,`sort`) |
| GET    | `/rooms/{id}`           | Busca sala pelo id                           |
| GET    | `/rooms/{id}/meetings`  | Lista reuniões da sala (paginado)            |
| PATCH  | `/rooms/{id}`           | Atualiza parcialmente a sala                 |
| DELETE | `/rooms/{id}`           | Remove sala                                |

### Reuniões (`/meetings`)
| Método | Caminho          | Descrição                                                         |
|--------|------------------|-------------------------------------------------------------------|
| POST   | `/meetings`      | Cria reunião validando disponibilidade de sala e usuário          |
| GET    | `/meetings`      | Lista reuniões com paginação (`page`,`size`,`sort`)               |
| GET    | `/meetings/{id}` | Busca reunião pelo id                                             |
| PATCH  | `/meetings/{id}` | Atualiza dados da reunião com validações de conflitos             |
| DELETE | `/meetings/{id}` | Remove reunião                                                    |

## Testes

Os testes utilizam um banco H2 em memória configurado via `profile` `test`.
```bash
./mvnw test
```

## Variáveis de ambiente

Para ajustar o segredo do JWT basta definir `API_SECRET` (ou editar `api.secret` no `application.properties`). Durante os testes automatizados é usado um segredo próprio em `application-test.properties`.
