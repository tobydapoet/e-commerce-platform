# E-Commerce Backend

Spring Boot backend for a multi-store e-commerce platform. The project includes authentication, RBAC, store management, product catalog, inventory, cart, checkout, order management, reviews, coupons, wishlist, file uploads, and bank-transfer payment confirmation through a SePay webhook flow.

## Tech Stack

- Java 17
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Security + JWT
- OAuth2 login with Google
- Spring Data JPA + Hibernate
- MySQL 8
- Redis
- MapStruct
- Lombok
- Cloudinary
- JUnit 5 + Mockito
- Docker Compose

## Main Features

- Authentication with email/password, Google OAuth2, JWT access tokens, and refresh sessions.
- RBAC with roles, permissions, and role-permission mappings.
- Store lifecycle management for sellers.
- Product catalog with categories, variants, attributes, inventory, and image upload.
- Cart grouped by store, with cart item quantity updates and cleanup.
- Checkout flow that creates orders from selected cart items.
- Coupon validation for platform and store coupons.
- Order store status management.
- Review creation only for delivered order items.
- Payment records created only after a successful paid confirmation.
- SePay webhook endpoint for bank transfer confirmation.

## Payment Flow

Orders and payments are intentionally separate.

1. A customer creates an order.
2. The system returns the order code and total amount.
3. The customer transfers money using the order code in the transfer content.
4. SePay sends a webhook to the backend.
5. The backend extracts the order code, verifies the order and paid amount, then creates a `Payment` record with status `PAID`.

`OrderService.create()` does not create a payment record. Payment creation happens after payment confirmation through `PaymentService`.

Webhook endpoint:

```http
POST /webhooks/sepay
```

## Project Structure

```text
src/main/java/com/example/e_commerce
+-- config          Application and security configuration
+-- constant        Enums for roles, statuses, payment methods, etc.
+-- controller      REST API controllers
+-- dto             Request and response DTOs
+-- entity          JPA entities
+-- exception       Custom exceptions and global exception handling
+-- filter          JWT request filter
+-- handler         OAuth2 success handler
+-- mapper          MapStruct and manual mappers
+-- repository      Spring Data repositories
+-- service         Business logic
`-- utils           Utility classes
```

## Database Schema

```mermaid
erDiagram
    USERS {
        UUID id PK
        string name
        string email UK
        string password
        string provider_id
        string avatar
        string status
        datetime created_at
        datetime updated_at
    }

    ROLES {
        bigint id PK
        string user_role UK
        string description
    }

    PERMISSIONS {
        bigint id PK
        string name UK
    }

    USER_ROLES {
        bigint id PK
        UUID user_id FK
        bigint role_id FK
    }

    ROLE_PERMISSIONS {
        bigint id PK
        bigint role_id FK
        bigint permission_id FK
    }

    SESSIONS {
        bigint id PK
        UUID user_id FK
        string token UK
        datetime expires_at
        boolean revoked
        datetime revoked_at
        datetime created_at
        datetime updated_at
    }

    ADDRESSES {
        bigint id PK
        UUID user_id FK
        string name
        string phone
        string address
        boolean is_default
        datetime created_at
        datetime updated_at
        datetime deleted_at
    }

    STORES {
        bigint id PK
        UUID owner_id FK
        string name
        string description
        string logo
        string banner
        string phone
        string email
        string status
        datetime created_at
        datetime updated_at
    }

    CATEGORIES {
        bigint id PK
        bigint parent_id FK
        string name UK
        string description
        string status
        datetime created_at
        datetime updated_at
    }

    PRODUCTS {
        bigint id PK
        bigint store_id FK
        bigint category_id FK
        string name
        text description
        string thumbnail
        string status
        double average_rating
        int sold_count
        datetime created_at
        datetime updated_at
    }

    ATTRIBUTES {
        bigint id PK
        bigint product_id FK
        string name
    }

    ATTRIBUTE_VALUES {
        bigint id PK
        bigint attribute_id FK
        string attribute_value
    }

    PRODUCT_VARIANTS {
        bigint id PK
        bigint product_id FK
        string sku UK
        decimal price
        string image
        string status
        datetime created_at
        datetime updated_at
    }

    PRODUCT_VARIANT_ATTRIBUTE_VALUES {
        bigint id PK
        bigint product_variant_id FK
        bigint attribute_value_id FK
    }

    INVENTORIES {
        bigint id PK
        bigint product_variant_id FK
        int quantity
        datetime created_at
        datetime updated_at
    }

    CART_STORES {
        bigint id PK
        UUID user_id FK
        bigint store_id FK
        bigint store_coupon_id FK
        datetime created_at
        datetime updated_at
    }

    CART_ITEMS {
        bigint id PK
        bigint cart_store_id FK
        UUID user_id FK
        bigint product_variant_id FK
        int quantity
        datetime created_at
        datetime updated_at
    }

    COUPONS {
        bigint id PK
        bigint store_id FK
        string code
        string creator_type
        string discount_type
        decimal discount_value
        decimal minimum_order
        decimal maximum_discount
        bigint quantity
        date start_date
        date end_date
        string status
        datetime created_at
        datetime updated_at
    }

    ORDERS {
        bigint id PK
        UUID user_id FK
        bigint address_id FK
        bigint platform_coupon_id FK
        string order_code UK
        decimal subtotal
        decimal discount
        decimal total
        datetime created_at
        datetime updated_at
    }

    ORDER_STORES {
        bigint id PK
        bigint order_id FK
        bigint store_id FK
        bigint store_coupon_id FK
        string status
        decimal subtotal
        decimal discount
        decimal shipping_fee
        decimal total
        string note
        datetime created_at
        datetime updated_at
    }

    ORDER_ITEMS {
        bigint id PK
        bigint order_store_id FK
        bigint product_variant_id FK
        int quantity
        decimal price
        decimal subtotal
    }

    PAYMENTS {
        bigint id PK
        bigint order_id FK
        string payment_method
        string payment_status
        string transaction_code UK
        decimal amount
        datetime paid_at
        datetime created_at
    }

    REVIEWS {
        bigint id PK
        UUID user_id FK
        bigint product_id FK
        bigint order_item_id FK
        int rating
        text comment
        datetime created_at
    }

    WISHLISTS {
        bigint id PK
        UUID user_id FK
        bigint product_id FK
        datetime created_at
    }

    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned
    ROLES ||--o{ ROLE_PERMISSIONS : has
    PERMISSIONS ||--o{ ROLE_PERMISSIONS : grants
    USERS ||--o{ SESSIONS : owns
    USERS ||--o{ ADDRESSES : owns
    USERS ||--o{ STORES : owns
    USERS ||--o{ CART_STORES : owns
    USERS ||--o{ CART_ITEMS : owns
    USERS ||--o{ ORDERS : places
    USERS ||--o{ REVIEWS : writes
    USERS ||--o{ WISHLISTS : saves

    CATEGORIES ||--o{ CATEGORIES : parent
    CATEGORIES ||--o{ PRODUCTS : groups
    STORES ||--o{ PRODUCTS : sells
    STORES ||--o{ COUPONS : owns
    STORES ||--o{ CART_STORES : groups
    STORES ||--o{ ORDER_STORES : fulfills

    PRODUCTS ||--o{ ATTRIBUTES : defines
    ATTRIBUTES ||--o{ ATTRIBUTE_VALUES : contains
    PRODUCTS ||--o{ PRODUCT_VARIANTS : has
    PRODUCT_VARIANTS ||--o{ PRODUCT_VARIANT_ATTRIBUTE_VALUES : maps
    ATTRIBUTE_VALUES ||--o{ PRODUCT_VARIANT_ATTRIBUTE_VALUES : selected
    PRODUCT_VARIANTS ||--|| INVENTORIES : stocks
    PRODUCT_VARIANTS ||--o{ CART_ITEMS : added
    PRODUCT_VARIANTS ||--o{ ORDER_ITEMS : ordered
    PRODUCTS ||--o{ REVIEWS : receives
    PRODUCTS ||--o{ WISHLISTS : saved

    COUPONS ||--o{ CART_STORES : applied
    COUPONS ||--o{ ORDERS : platform_discount
    COUPONS ||--o{ ORDER_STORES : store_discount

    CART_STORES ||--o{ CART_ITEMS : contains
    ADDRESSES ||--o{ ORDERS : ships_to
    ORDERS ||--o{ ORDER_STORES : splits
    ORDERS ||--|| PAYMENTS : paid_by
    ORDER_STORES ||--o{ ORDER_ITEMS : contains
    ORDER_ITEMS ||--o| REVIEWS : reviewed_by
```

## Environment Variables

Create a `.env` file or export these variables in your environment:

```env
DB_URL=jdbc:mysql://localhost:3308/e_sql
DB_USERNAME=root
DB_PASSWORD=your_database_password

JWT_SECRET=your_32_byte_or_longer_secret

GOOGLE_CLIENT=your_google_client_id
GOOGLE_SECRET=your_google_client_secret
REDIRECT_URI={baseUrl}/login/oauth2/code/{registrationId}

CLOUD_NAME=your_cloudinary_cloud_name
API_KEY=your_cloudinary_api_key
API_SECRET=your_cloudinary_api_secret

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

EMAIL_PORT=587
EMAIL_USERNAME=your_email
EMAIL_PASSWORD=your_app_password
```

Do not commit real credentials to source control.

## Run Locally

Start MySQL with Docker Compose:

```powershell
docker compose up -d e-sql
```

Run the application:

```powershell
.\mvnw.cmd spring-boot:run
```

Or with local Maven:

```powershell
mvn spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

Health check:

```http
GET /actuator/health
```

## Run With Docker Compose

```powershell
docker compose up --build
```

This starts MySQL and the backend application.

## Testing

Run all unit/service tests:

```powershell
mvn "-Dtest=*ServiceTest" test
```

Run a specific test class:

```powershell
mvn "-Dtest=PaymentServiceTest" test
```

Run the full test suite:

```powershell
mvn test
```

The test profile uses H2 in-memory database for the Spring context test.

## Important API Areas

- `POST /auth/register`
- `POST /auth/login`
- `POST /orders`
- `GET /orders/me`
- `GET /orders/me/{id}`
- `POST /reviews`
- `GET /reviews/products/{productId}`
- `DELETE /reviews/{id}`
- `POST /webhooks/sepay`

Some endpoints require JWT authentication. The SePay webhook endpoint is publicly accessible because it is called by an external payment service.

## Notes

- Database schema is managed by Hibernate with `spring.jpa.hibernate.ddl-auto=update` in the default profile.
- RBAC seed data is initialized by `DataInitializer`.
- Cloudinary is used for product and store image uploads.
- Redis is configured for application infrastructure support.
- The current payment implementation supports confirmed bank transfers and stores successful transactions as `Payment` rows.
