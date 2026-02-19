# Payment Gateway Service

### Functional Requirement
This service is responsible for providing API  to merchants for 
1. A merchant should be able to process a payment through the payment gateway
2. A merchant should be able to retrieve the details of a previously made payment


### Assumptions
1. USD,GBP and EUR are the only supported currencies
2. Real time Sync processing is supported. If accquiring bank API is not available then provides REJECTION instead of processing the payment later.
3. Retry for bank API is not considered.
4. Observability is not considered for this implementation.
5. APIs Authentication & Authorization are considered out of scope.


## API Design
### 1. Process Payment API ```POST /api/payments```
- Design Consideration
  - Validation of all input parameters
  - Idempotency support via Idempotency-Key header so retries never double‑charge
  - State management for payment request
-  [Create Payment API Doc](http://localhost:8090/swagger-ui/index.html#/payment-gateway-controller/processPayment)

### 2. Get Payment API ```GET /api/payments/{paymentId}```
-  [Create Payment API Doc](http://localhost:8090/swagger-ui/index.html#/payment-gateway-controller/getPostPaymentEventById)

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

## Testing
- Junit Tests are written for controller and service layer
- Integration test is also written


## Nice to have
1. Process the payments later which are failed due to Acquire Bank API unavailability.
2. Retry and exponentially backoff policy for calling Acquire Bank API
3. Make the whole payment process Async and process the payment by considering the rate limits of Acquire Bank API(if exists). Tradeoff is -  client need to call the get api to get the status of payment
4. Data masking in logs for all sensitive data like cardNUmber, expiry, CVV etc
5. API should be secured by API keys 