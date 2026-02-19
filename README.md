# Payment Gateway Service

This service is responsible for providing API  to merchants for 
1.  A merchant should be able to process a payment through the payment gateway
2. A merchant should be able to retrieve the details of a previously made payment


## API Design
### 1. Process Payment API ```POST /api/payments```
- Design Consideration
  - Validation of all input parameters
  - Idempotency support via Idempotency-Key header so retries never double‑charge
  - State management for payment request


## API Documentation
For documentation openAPI is included, and it can be found under the following url: **http://localhost:8090/swagger-ui/index.html**


## Requirements
- JDK 17
- Docker


## Template structure

src/ - A skeleton SpringBoot Application

test/ - Some simple JUnit tests

imposters/ - contains the bank simulator configuration. Don't change this

.editorconfig - don't change this. It ensures a consistent set of rules for submissions when reformatting code

docker-compose.yml - configures the bank simulator


## API Documentation
For documentation openAPI is included, and it can be found under the following url: **http://localhost:8090/swagger-ui/index.html**

**Feel free to change the structure of the solution, use a different library etc.**