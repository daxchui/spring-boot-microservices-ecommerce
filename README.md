# spring-boot-microservices-ecommerce

This repository contains the source code for an online store system built using a microservice architecture with Spring Boot and Gradle. The system demonstrates principles of fault-tolerance, availability, and eventual consistency through asynchronous messaging.

## System Architecture

The system is composed of four independent microservices that communicate with each other via RabbitMQ:

| Service      | Port | Description                                                                 |
|--------------|------|-----------------------------------------------------------------------------|
| `store`      | 8080 | Main application handling customer registration, orders, and UI coordination.   |
| `bank`       | 8081 | Handles payment transfers and refunds with ACID guarantees.                 |
| `deliveryco` | 8082 | Manages the shipment and delivery process.                                  |
| `emailservice`| 8083 | Handles sending notifications for various events (e.g., order confirmation). |

### Key Design Features

- **Microservice Architecture:** Each service is a separate Spring Boot application, allowing for independent development, deployment, and scaling.
- **Asynchronous Communication:** Services communicate primarily through RabbitMQ, which decouples them and improves fault tolerance. For example, if the `emailservice` is down, order processing can continue, and emails will be sent once the service recovers.
- **Customer Account Mapping:** When a customer is created in the `store` service, a corresponding bank account is automatically created in the `bank` service via a REST API call. The `store` service stores the `bankAccountId` to link the two, ensuring payments are charged to the correct account.
- **Database-backed Email Service:** The `emailservice` now persists all outgoing emails to a database. A scheduled process sends pending emails, ensuring no notifications are lost if the service restarts.
- **Fault-Tolerant Delivery:** The `deliveryco` service uses an asynchronous, background process to handle the multi-step delivery simulation, preventing the main message listener from blocking or failing.
- **Stock Allocation:** The system correctly tracks which warehouse provides stock for an order, ensuring that if an order is cancelled, the stock is returned to the correct original warehouse.

---

## Prerequisites

| Tool         | Version        | Notes                               |
|--------------|----------------|-------------------------------------|
| Java JDK     | 17 or higher   | Required for Spring Boot 3.3+       |
| Docker       | Latest         | For running RabbitMQ                |
| Git          | Latest         | For version control                 |
| IDE          | IntelliJ IDEA or VSCode is recommended |

---

## Getting Started

Follow these steps to get the complete system running.

### 1. Clone the Repository
```bash
git clone <repo-url>
cd <repo-directory>
```

### 2. Start RabbitMQ

Run the following command in your terminal to start a RabbitMQ container:

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3.13-management
```

You can access the RabbitMQ Management UI at [http://localhost:15672](http://localhost:15672) (login with `guest`/`guest`).

### 3. Create PostgreSQL Databases

Each service requires its own database. Connect to your PostgreSQL instance (e.g., using `psql`) and run the following commands:

```sql
CREATE DATABASE store;
CREATE DATABASE bank;
CREATE DATABASE deliveryco;
CREATE DATABASE emailservice;
```

**Note:** The `application.properties` file for each service is pre-configured to connect to these databases on `localhost:5432`. Please fill in the username and password.

### 4. Build and Run the Microservices

For **each** of the four service directories (`store`, `bank`, `deliveryco`, `emailservice`), run the following commands. It's best to use a separate terminal for each service.

```bash
# Example for the 'store' service
cd store
gradle wrapper --gradle-version 8.9
./gradlew clean build
./gradlew bootRun
```

Repeat this for the other three services:

```bash
# In a new terminal
cd bank
gradle wrapper --gradle-version 8.9
./gradlew clean build
./gradlew bootRun
```

- The `bank` service will initialize its schema using **Flyway**.
- The other services will initialize their schemas using **Hibernate (`ddl-auto`)**.

### 5. Run the Frontend

Once all backend services are running, start the simple web server for the frontend.

```bash
cd store/frontend
python3 -m http.server 5500
```

Now, open your browser and navigate to **[http://localhost:5500](http://localhost:5500)**.

### 6. Test the Application

1.  **Sign Up:** Use the "Sign Up" button to create a new customer. This will also create a linked bank account with a starting balance of $10,000.
2.  **Login:** Log in with the new user's credentials.
3.  **Place an Order:** Select a product and place an order. You should see the status updates in the UI as the order is processed by the different services.
4.  **Cancel an Order:** You can cancel an order while it is in the `PROCESSING` state.