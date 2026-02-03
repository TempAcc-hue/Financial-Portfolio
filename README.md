# Financial Portfolio Manager

A full-stack financial portfolio management application built with Java Spring Boot, MySQL, and a modern JavaScript frontend.

## Features

- **Portfolio Dashboard**: View total portfolio value, gain/loss, and asset allocation
- **Asset Management**: Add, edit, and delete various asset types (stocks, bonds, ETFs, mutual funds, crypto, real estate)
- **Real-time Stock Prices**: Integration with Yahoo Finance for live stock prices
- **Interactive Charts**: Portfolio allocation pie chart and performance bar chart using ApexCharts
- **REST API**: Full CRUD API with Swagger documentation
- **Responsive Design**: Modern dark theme with glassmorphism effects

## Tech Stack

- **Backend**: Java 21, Spring Boot 4.x, Spring Data JPA
- **Database**: MySQL 8.x
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Charts**: ApexCharts
- **API Docs**: Swagger/OpenAPI (springdoc)

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- MySQL 8.0+

## Setup Instructions

### 1. Database Setup

Create the MySQL database (or let the app create it automatically):

```bash
mysql -u root -p < src/main/resources/db/setup.sql
```

### 2. Configure Database Connection

Update the MySQL password in `src/main/resources/application.properties`:

```properties
spring.datasource.password=YOUR_PASSWORD
```

### 3. Build and Run

```bash
# Navigate to the demo folder
cd demo

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

### 4. Access the Application

- **Dashboard**: http://localhost:8080/
- **Swagger API Docs**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## API Endpoints

### Assets

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/assets` | Get all assets |
| GET | `/api/assets/{id}` | Get asset by ID |
| GET | `/api/assets/type/{type}` | Get assets by type |
| GET | `/api/assets/search?q=query` | Search assets |
| POST | `/api/assets` | Create new asset |
| PUT | `/api/assets/{id}` | Update asset |
| DELETE | `/api/assets/{id}` | Delete asset |

### Portfolio

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/portfolio/summary` | Get portfolio summary |
| GET | `/api/portfolio/allocation` | Get allocation breakdown |
| GET | `/api/portfolio/performance` | Get performance by type |

## Sample API Request

```bash
# Create a new stock
curl -X POST http://localhost:8080/api/assets \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "NVDA",
    "name": "NVIDIA Corporation",
    "type": "STOCK",
    "quantity": 10,
    "buyPrice": 450.00,
    "purchaseDate": "2024-01-15"
  }'
```

## Project Structure

```
demo/
├── src/main/java/com/example/demo/
│   ├── config/          # Swagger and Web configuration
│   ├── controller/      # REST controllers
│   ├── dto/             # Data transfer objects
│   ├── entity/          # JPA entities
│   ├── exception/       # Exception handling
│   ├── repository/      # JPA repositories
│   └── service/         # Business logic
├── src/main/resources/
│   ├── static/          # Frontend files (HTML, CSS, JS)
│   ├── db/              # Database setup scripts
│   └── application.properties
└── pom.xml
```

## Asset Types

- `STOCK` - Stocks (e.g., AAPL, GOOGL)
- `BOND` - Bonds and bond ETFs
- `ETF` - Exchange-traded funds
- `MUTUAL_FUND` - Mutual funds
- `CRYPTO` - Cryptocurrencies
- `REAL_ESTATE` - Real estate investments
- `CASH` - Cash holdings

## License

MIT License
