## SnackOverFlow API
<img src="./SnackOverFlow.png" alt="SnackOverFlow" width="200" align="right">

### 🎯 Objective
Build a RESTful API for a restaurant order management system that demonstrates your ability to:
- Design scalable backend architectures
- Implement message-driven patterns using RabbitMQ
- Handle object storage using MinIO
- Write clean, maintainable code

### 📋 Business Requirements

#### 1. Menu Management System
The restaurant needs a comprehensive order management system with the following capabilities:

#### Menu Item Properties
- **Basic Information:** name, description, price, category
- **Operational Data:** preparation time (minutes), availability status
- **Nutritional Information:** calories per serving
- **Menu Images:** images stored in object storage

#### Categories
- `APPETIZER` - Starters and small plates
- `MAIN` - Main courses
- `DESSERT` - Sweet items
- `BEVERAGE` - Drinks (hot/cold)

### 2. Order Processing Pipeline
Implement a complete order lifecycle management system:

#### Order Flow
```
PENDING → CONFIRMED → PREPARING → READY
              ↓
          CANCELLED (allowed only before PREPARING)
```

#### Business Rules
- **Minimum Order Value:** 15 SAR
- **Auto-cancellation:** Orders not confirmed within 10 minutes
- **Preparation Time Calculation:** Maximum prep time among items, plus a 5 minute buffer
- **Concurrent Order Limit:** Maximum 20 active orders (CONFIRMED/PREPARING states)

### 3. Message Queue Processing
Every state change must publish a message to RabbitMQ for async processing:

#### Message Types
- `ORDER_PLACED` - New order created
- `ORDER_CONFIRMED` - Order confirmed
- `ORDER_STATUS_CHANGED` - Any status transition
- `ORDER_CANCELLED` - Order cancelled (include the reason in notes)

### 🔧 Technical Requirements

### Core Technology Stack
|     Component    |        Technology       |
|------------------|-------------------------|
|     Language     |           Java          |
|     Framework    |        Spring Boot      |
|     Database     |        PostgreSQL       |
|  Message Broker  |         RabbitMQ        |
|  Object Storage  |          MinIO          |
| Containerization | Docker & Docker Compose |

### API Endpoints
```yaml
# Menu Management
GET    /api/v1/menu                    # List items (paginated, filterable)
GET    /api/v1/menu/{id}               # Get item details
POST   /api/v1/menu                    # Create item
PUT    /api/v1/menu/{id}               # Update item
DELETE /api/v1/menu/{id}               # Soft delete
PATCH  /api/v1/menu/{id}/availability  # Toggle availability

# Order Management  
POST   /api/v1/orders                  # Place order
GET    /api/v1/orders/{id}             # Get order details
GET    /api/v1/orders                  # List orders (filtered)
PATCH  /api/v1/orders/{id}/status      # Update status
POST   /api/v1/orders/{id}/cancel      # Cancel order

# Customer Operations
GET    /api/v1/customers/{id}/orders   # Order history
POST   /api/v1/customers               # Register customer
```

### RabbitMQ Configuration

### Exchange & Queue Setup
**Name:** `orders.exchange`  
**Durable:** true  
**Queues:**
```
orders.placed.queue       # Routing key: order.placed
orders.status.queue       # Routing key: order.status.*
orders.notification.queue # Routing key: order.#
```

### Requirements:
- Implement ≥ 1 consumer to handle order processing.
- Use acknowledgment modes properly
- Implement Dead Letter Queue (DLQ) for failed messages
- Handle duplicate messages (idempotency)

### Sample Message
```json
{
  "messageId": "uuid",
  "messageType": "ORDER_STATUS_CHANGED",
  "timestamp": "2025-09-12T10:50:00Z",
  "payload": {
    "orderId": "uuid",
    "customerId": "uuid",
    "previousStatus": "PENDING",
    "currentStatus": "CONFIRMED",
    "metadata": {
      "preparationTime": 35,
      "amount": 28.00,
      "itemCount": 3
    }
  }
}
```

### Object Storage Requirements
- Server: MinIO
- Max file size: 5MB
- Supported formats: JPEG, PNG, SVG

### ☸️ Kubernetes Deployment
In addition to `docker-compose`, the whole system must run on a local Kubernetes cluster, with the API running as **2 replicas**.

#### Cluster Setup
- Preferbly use [kind](https://kind.sigs.k8s.io/) and commit its config as `k8s/kind-config.yaml`.
- Provide one command to bring the system up and one to tear it down. On a machine with only Docker, kind and kubectl installed, `make k8s-up` must work with no manual steps:

```bash
make k8s-up    # create cluster, build & load image, apply manifests, wait until ready
make k8s-down  # delete the cluster
```

- Build the API image locally and load it with `kind load docker-image`. Do not push it to a public registry.
- Tag the image with a fixed version (e.g. `snackoverflow-api:1.0.0`), never `latest`.
- Expose the API to the host through a NodePort with `extraPortMappings`, or an Ingress/Gateway, bound to `127.0.0.1`. Document the URL.

#### Manifests
All manifests live in `k8s/` and are applied with a single `kubectl apply -k k8s/` (Kustomize), into the `snackoverflow` namespace.

| Resource | Requirement |
|----------|-------------|
| API Deployment | `replicas: 2`, rolling update |
| API Service | `ClusterIP`, plus the host exposure above |
| ConfigMap | Non-sensitive configuration (URLs, exchange/queue names, limits) |
| Secret | Credentials, generated with a Kustomize `secretGenerator` |
| PostgreSQL, RabbitMQ, MinIO | One `StatefulSet` with a `PersistentVolumeClaim` each. A single replica is fine; high availability is not expected |

- Use official upstream images with pinned tags.
- Do not commit real credentials. Generate the Secret from a gitignored env file, and commit an example (e.g. `k8s/.env.example`).
- Change the default MinIO and RabbitMQ credentials.

#### API Deployment Requirements
- **Probes:** separate liveness and readiness probes. Liveness must not depend on PostgreSQL, RabbitMQ or MinIO.
- **Resources:** CPU and memory requests and limits.
- **Security context:** run as non-root, drop all capabilities, and disallow privilege escalation.

#### Running with 2 Replicas
The business rules must hold while both replicas are serving traffic:
- The **Concurrent Order Limit** is never exceeded by simultaneous requests hitting different replicas.
- **Auto-cancellation** handles each order once, not once per replica.
- Consumers on both replicas share the queues without processing a message twice.
- If you implement the caching bonus, one replica does not serve data the other has already changed.

Explain in your documentation how your design handles each of these.

#### How We Will Verify
- Run `make k8s-up` on a clean machine and wait for all pods to become ready.
- Run your API requests collection against the exposed URL.
- Delete an API pod while sending requests, then roll out a new image version, and check that the system keeps serving requests.

### ✨ Bonus
- Add caching layer when listing menu items
- Package the API manifests as a Helm chart

### 📝 Submission
1. Push your code to a GitHub **private** repository, and assign this account as a collaborator. To assign, go to repository's settings > Collaborators > Add people and search for **NCAI-SWE**.
2. Provide a project documentation, and include any assumptions made.
3. Ensure `docker-compose up` starts the entire system.
4. Provide API requests collection.
5. Ensure your Kubernetes script or `make` target starts the entire system on kind.

Remember, we value **quality over quantity**. A well-designed **partial** solution with clear documentation of what remains is better than a complete but **poorly** architected system.

### Good Luck! 🚀
