# SnackOverFlow Setup Guide

This guide explains how to start the application and test available endpoints.

## 1) Prerequisites

- Docker Desktop (or Docker Engine + Compose plugin)
- Optional for local app run: JDK 21

## 2) Service Ports

- App: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
- RabbitMQ AMQP: `localhost:5672`
- RabbitMQ UI: `http://localhost:15672`
- MinIO API: `http://localhost:9000`
- MinIO Console: `http://localhost:9001`

## 3) Start the Full Stack (Docker Compose)

Run from project root:

```powershell
docker compose down -v
docker compose up -d --build
```

Check container status:

```powershell
docker compose ps
```

Watch app logs:

```powershell
docker compose logs -f app
```

## 4) Current Default Runtime Values

### Database

- DB name: `snackoverflow_db` (auto-created by Docker Compose)
- User: `admin`
- Password: `123`
- App schema: `snackoverflow` (auto-created by Liquibase)
- Liquibase schema: `liquibase_meta` (auto-created by Liquibase)

### RabbitMQ

- User: `guest`
- Password: `guest`

### MinIO

- Access key: `minioadmin`
- Secret key: `minioadmin`
- Bucket: `snackoverflow`

## 5) Verify the Application Is Up

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/actuator/health"
```

You should see a response with `status` equal to `UP` when dependencies are reachable.

## 6) API Requests (Implemented Endpoints)

Current implemented HTTP endpoints are under menu.

Base URL:

- `http://localhost:8080/api/v1/menu`

### Create menu item

**PowerShell:**
```powershell
$body = @{
  name = "Cheese Burger"
  description = "Beef burger with cheese"
  price = 26.50
  category = "MAIN"
  preparationTimeMinutes = 15
  isAvailable = $true
  caloriesPerServing = 680
} | ConvertTo-Json

Invoke-RestMethod -Method Post \
  -Uri "http://localhost:8080/api/v1/menu" \
  -ContentType "application/json" \
  -Body $body
```

**Raw JSON:**
```json
{
  "name": "Cheese Burger",
  "description": "Beef burger with cheese",
  "price": 26.50,
  "category": "MAIN",
  "preparationTimeMinutes": 15,
  "isAvailable": true,
  "caloriesPerServing": 680
}
```

### List menu items

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/menu"
```

### List with filter

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/menu?category=MAIN&available=true"
```

### Get menu item by ID

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/menu/{id}"
```

### Update menu item

**PowerShell:**
```powershell
$update = @{
  description = "Updated description"
  price = 28.00
} | ConvertTo-Json

Invoke-RestMethod -Method Put \
  -Uri "http://localhost:8080/api/v1/menu/{id}" \
  -ContentType "application/json" \
  -Body $update
```

**Raw JSON:**
```json
{
  "description": "Updated description",
  "price": 28.00
}
```

### Toggle availability

```powershell
Invoke-RestMethod -Method Patch -Uri "http://localhost:8080/api/v1/menu/{id}/availability"
```

### Soft delete

```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/v1/menu/{id}"
```

### Upload image

**cURL:**
```powershell
curl -X POST "http://localhost:8080/api/v1/menu/{id}/image" -F "file=@C:/path/to/image.png"
```

## 7) RabbitMQ Verification

Open UI:

- URL: `http://localhost:15672`
- Login: `guest` / `guest`

Check the following are present:

- Exchange: `orders.exchange`
- Queues:
  - `orders.placed.queue`
  - `orders.status.queue`
  - `orders.notification.queue`

## 8) MinIO Verification

Open console:

- URL: `http://localhost:9001`
- Login: `minioadmin` / `minioadmin`

After image upload, check bucket:

- `snackoverflow`

**Note on Database/Schema Creation:** The `snackoverflow_db` database and `liquibase_meta` schema are created automatically by Docker Compose and Liquibase, respectively. No manual setup is required.

## 9) Optional: Run App Locally (without app container)

If Postgres/RabbitMQ/MinIO are already running:

```powershell
.\mvnw.cmd spring-boot:run
```

## 10) Stop/Cleanup

Stop containers:

```powershell
docker compose down
```

Stop and remove volumes:

```powershell
docker compose down -v
```

## 11) Known Gap

`OrderController` is currently a placeholder class, so order/customer HTTP endpoint testing should be added after controller implementation.
