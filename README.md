# Product service + MCP server

Spring Boot 4.2.0, Java 21, Maven.

| Module | Port | Purpose |
|---|---|---|
| `product-service` | 8080 | REST API: create / read / list / delete Product, JWT login |
| `product-mcp-server` | 8081 | MCP server (Streamable HTTP, `/mcp`) exposing the API as tools |

## Project structure

```
product-service/src/main/java/com/example/product
  controller/   ProductController, AuthController          (HTTP only, no logic)
  service/      ProductService, AuthService, JwtService,   (business logic)
                CustomUserDetailsService
  repository/   ProductRepository, UserAccountRepository  (Spring Data JPA)
  entity/       Product, UserAccount, Role                 (JPA model)
  dto/          ProductRequest/Response, LoginRequest, TokenResponse
  config/       SecurityConfig, JwtConfig, DataSeeder
  exception/    ProductNotFoundException, GlobalExceptionHandler

product-mcp-server/src/main/java/com/example/mcp
  tool/         ProductTools        (the MCP tools Claude sees)
  client/       ProductApiClient    (calls product-service, forwards the caller's JWT)
  config/       SecurityConfig, McpConfig
```

## Roles

| Action | USER | ADMIN |
|---|---|---|
| POST /api/products, GET /api/products, GET /api/products/{id} | yes | yes |
| DELETE /api/products/{id} | 403 | yes |

Seeded accounts: `user / user123` (USER), `admin / admin123` (ADMIN). Override with `SEED_USER_PASSWORD` / `SEED_ADMIN_PASSWORD`.

## Run

1. Database. Default is PostgreSQL (`docker-compose.yml` provides one, or point `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` at your own).
   With no Postgres, add `--spring.profiles.active=h2` for an in-memory database.
2. `cd product-service && ./mvnw spring-boot:run` (add `-Dspring-boot.run.profiles=h2` for H2)
3. `cd product-mcp-server && ./mvnw spring-boot:run`

Both services must share `JWT_SECRET` (32+ bytes). The default is for local dev only.

## Get a token

```
curl -X POST localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Tokens last 60 minutes (`JWT_EXPIRY_MINUTES`).

## Connect Claude

The MCP endpoint needs the JWT as a Bearer header. The token's role decides what the tools may do,
because the MCP server forwards it to product-service.

Claude Code:

```
claude mcp add --transport http product http://localhost:8081/mcp --header "Authorization: Bearer <TOKEN>"
```

Claude Desktop (remote servers with custom headers go through the `mcp-remote` bridge; needs Node.js).
In `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "product": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://localhost:8081/mcp",
               "--header", "Authorization:${AUTH_HEADER}"],
      "env": { "AUTH_HEADER": "Bearer <TOKEN>" }
    }
  }
}
```

Use a USER token to see the 403 on delete, an ADMIN token to delete. Re-issue and re-add the token when it expires.

## Tools

`listProducts`, `getProduct(id)`, `createProduct(name, description?, price, quantity)`, `deleteProduct(id)`.
