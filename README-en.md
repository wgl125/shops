# E-commerce Website Project

## Project Description
A lightweight shopping system developed using native Java technology stack. The project adopts a pure backend API architecture without relying on heavyweight frameworks like Spring, providing complete user management, product management, shopping cart, and order processing functions.

## Technology Stack
- Backend: Java Servlet + JDBC
- Database: MySQL
- Java Environment: JDK 17, Maven 3.8+ (Build Tool)
- Web Container: Jakarta EE 6.0 (Servlet 6.0), Tomcat 10+

## Configuration
Database configuration location: `src/main/resources/config/database.properties`

## Project Structure
```
student_shops/
├── src/                    # Source code directory
│   ├── main/              # Main code
│   │   ├── java/         # Java source code
│   │   │   └── com/shop/
│   │   │       ├── controller/    # API controllers
│   │   │       ├── service/      # Business logic layer
│   │   │       ├── dao/         # Data access layer
│   │   │       ├── model/       # Data models
│   │   │       ├── util/        # Utility classes
│   │   │       └── filter/      # Filters
│   │   ├── resources/   # Resource directory
│   │   │   ├── config/  # Configuration files
│   │   │   └── sql/     # SQL scripts
│   │   └── webapp/     # Web application directory
│   │       ├── WEB-INF/
│   │       │   └── web.xml  # Web configuration
│   │       └── static/   # Static resources
│   │           ├── images/  # Image files
│   │           │   ├── products/  # Product images
│   │           │   └── upload/    # Upload temp directory
│   │           ├── js/      # JavaScript files
│   │           └── css/     # CSS style files
│   └── test/              # Test code
├── pom.xml               # Maven configuration
└── README.md            # Project documentation
```

## File Upload Configuration
- Product image upload path: `/static/images/products/`
- Temporary file upload path: `/static/images/upload/`
- Supported image formats: jpg, jpeg, png
- Maximum file size: 5MB
- Image access URL format: `http://domain/images/products/image-name`

## Features
- User Management
  - User registration and login
  - Role-based access control
  - JWT authentication
  
- Product Management
  - Product CRUD operations
  - Category management
  - Product image upload
  
- Shopping Cart
  - Add/remove items
  - Update quantities
  - Calculate totals
  
- Order Processing
  - Create orders
  - Order status management
  - Order history

## API Documentation
All APIs return JSON format responses with the following structure:
```json
{
    "code": 200,          // Status code
    "message": "success", // Status message
    "data": {            // Response data
        // Specific response data
    }
}
```

### Common Status Codes
- 200: Success
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error
