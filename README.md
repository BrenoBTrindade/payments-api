# 💳 Payments API
API REST desenvolvida em **Spring Boot** para gerenciamento de pagamentos.  
Permite **criar, listar, filtrar, atualizar status** e **inativar (soft delete)** pagamentos, com regras de negócio bem definidas.

## 🧩 Funcionalidades principais
- Criar pagamento  
- Listar todos os pagamentos  
- Filtrar pagamentos por código de débito, CPF/CNPJ ou status  
- Buscar pagamento por ID  
- Atualizar status do pagamento com regras de transição  
- Exclusão lógica (soft delete) — inativa o pagamento (não remove do banco)

## 🛠 Tecnologias utilizadas
- Java 21  
- Spring Boot 3.x  
- Spring Data JPA  
- H2 Database (em memória)  
- Maven  

## 🔧 Como executar localmente
1. Clone o repositório:
   ```bash
   git clone https://github.com/seuusuario/payments-api.git
   cd payments-api
   ```
2. Compile e execute:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
3. A aplicação estará disponível em:
   ```
   http://localhost:8080
   ```
4. Para acessar o **H2 Console**:
   ```
   http://localhost:8080/h2-console
   ```
   - **JDBC URL:** `jdbc:h2:mem:testdb`  
   - **Usuário:** `sa`  
   - **Senha:** *(em branco)*  

## 💾 Banco de Dados (H2 + DataLoader)
O projeto inclui a classe `DataLoader` que popula o banco H2 automaticamente com dados iniciais sempre que a aplicação é iniciada.

### Registros iniciais
| ID | codigoDebito | cpfCnpjPagador  | metodoPagamento | numeroCartao        | valor   | status               | ativo |
|----|--------------|------------------|------------------|---------------------|---------|----------------------|-------|
| 1  | 1001         | 12345678900      | boleto           | null                | 150.00  | PENDENTE             | true  |
| 2  | 1002         | 98765432100      | cartao_credito   | 4111111111111111    | 320.75  | PENDENTE             | true  |
| 3  | 1003         | 56789012345      | cartao_debito    | 5500000000000004    | 89.90   | PROCESSADO_FALHA     | true  |
| 4  | 1004         | 10293847566      | pix              | null                | 999.99  | PROCESSADO_SUCESSO   | true  |

## 📡 Endpoints
> Base URL: `http://localhost:8080/api/payments`

### 🟢 Criar pagamento
**POST** `/api/payments`
```bash
curl -X POST http://localhost:8080/api/payments -H "Content-Type: application/json" -d '{"codigoDebito": 2001, "cpfCnpjPagador": "11222333000181", "metodoPagamento": "boleto", "valor": 1250.00}'
```

### 🔵 Listar pagamentos
**GET** `/api/payments`
```bash
curl -X GET "http://localhost:8080/api/payments"
curl -X GET "http://localhost:8080/api/payments?codigoDebito=1002"
curl -X GET "http://localhost:8080/api/payments?cpfCnpjPagador=98765432100"
curl -X GET "http://localhost:8080/api/payments?status=PENDENTE"
```

### 🟣 Buscar por ID
**GET** `/api/payments/{id}`
```bash
curl -X GET http://localhost:8080/api/payments/1
```

### 🟠 Atualizar status
**PUT** `/api/payments/{id}/status`
```bash
curl -X PUT http://localhost:8080/api/payments/1/status -H "Content-Type: application/json" -d '{"novoStatus":"PROCESSADO_SUCESSO"}'
curl -X PUT http://localhost:8080/api/payments/2/status -H "Content-Type: application/json" -d '{"novoStatus":"PROCESSADO_FALHA"}'
curl -X PUT http://localhost:8080/api/payments/3/status -H "Content-Type: application/json" -d '{"novoStatus":"PENDENTE"}'
```

### 🔴 Exclusão lógica
**DELETE** `/api/payments/{id}`
```bash
curl -X DELETE http://localhost:8080/api/payments/2
```

## ⚙️ Regras de transição de status
| Status atual | Pode mudar para |
|---------------|----------------|
| `PENDENTE` | `PROCESSADO_SUCESSO` ou `PROCESSADO_FALHA` |
| `PROCESSADO_FALHA` | `PENDENTE` |
| `PROCESSADO_SUCESSO` | ❌ Não pode ser alterado |

## ✅ Casos de teste sugeridos
1️⃣ Criar um novo pagamento via POST.  
2️⃣ Listar todos os pagamentos.  
3️⃣ Filtrar pagamentos por `codigoDebito`, `cpfCnpjPagador` e `status`.  
4️⃣ Buscar pagamento por ID.  
5️⃣ Atualizar status (testar transições válidas e inválidas).  
6️⃣ Inativar (soft delete) um pagamento com status `PENDENTE`.  
7️⃣ Verificar que o pagamento inativado ainda existe no banco (mas com `ativo = false`).
