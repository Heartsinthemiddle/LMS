# Course Package with Courses Assignment API Documentation

## Overview
Created a new API endpoint to create a course package and assign multiple courses to it in a single transaction.

## New Files Created

### 1. DTO Request: CreateCoursePackageWithCoursesRequest
**Location:** `/home/suraj/Documents/LMS/src/main/java/com/lms/dto/request/CreateCoursePackageWithCoursesRequest.java`

Contains all course package details plus a list of course IDs to be assigned:
- `packageName` - Name of the package
- `description` - Package description
- `monthlyPrice` - Monthly subscription price
- `yearlyPrice` - Yearly subscription price
- `stripeMonthlyPriceId` - Stripe price ID for monthly billing
- `stripeYearlyPriceId` - Stripe price ID for yearly billing
- `childLimit` - Maximum children allowed
- `courseLimit` - Maximum courses allowed
- `isActive` - Whether package is active
- `courseIds` - List of course IDs to assign (required, at least 1)

## Updated Files

### 1. CoursePackageService
**Location:** `/home/suraj/Documents/LMS/src/main/java/com/lms/service/CoursePackageService.java`

**New Method:** `createCoursePackageWithCourses(CreateCoursePackageWithCoursesRequest req)`

**Features:**
- Validates that courseIds list is not empty
- Validates that course limit is sufficient for the number of courses
- Verifies all courses exist in the database
- Checks that no selected courses are deleted
- Creates the course package
- Assigns all courses to the newly created package
- Uses @Transactional to ensure atomic operation
- Logs all operations for audit trail

**Validations:**
1. At least one course must be assigned
2. Number of courses cannot exceed course limit
3. All course IDs must exist
4. Selected courses cannot be deleted
5. Standard package validations (required fields, pricing, etc.)

### 2. CoursePackageController
**Location:** `/home/suraj/Documents/LMS/src/main/java/com/lms/controller/CoursePackageController.java`

**New Endpoint:** `POST /api/v1/course-packages/with-courses`

**Request Body:**
```json
{
  "packageName": "Premium Package",
  "description": "Complete course package",
  "monthlyPrice": 29.99,
  "yearlyPrice": 299.99,
  "stripeMonthlyPriceId": "price_monthly_123",
  "stripeYearlyPriceId": "price_yearly_456",
  "childLimit": 5,
  "courseLimit": 20,
  "isActive": true,
  "courseIds": [1, 2, 3, 4, 5]
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Course package created with courses assigned successfully",
  "data": {
    "id": 1,
    "packageName": "Premium Package",
    "description": "Complete course package",
    "monthlyPrice": 29.99,
    "yearlyPrice": 299.99,
    "childLimit": 5,
    "courseLimit": 20,
    "isActive": true,
    "createdAt": "2025-12-11T10:30:00",
    "createdBy": "admin",
    "isDeleted": false
  },
  "timestamp": "2025-12-11T10:30:00"
}
```

**Error Responses:**
- `400 Bad Request` - Missing courses, course limit exceeded, or invalid data
- `404 Not Found` - One or more courses not found
- `401 Unauthorized` - Invalid authentication token

## API Features

### 1. Transaction Support
- All course assignments happen within a single database transaction
- If any validation fails, no changes are committed
- Ensures data consistency

### 2. Validation & Error Handling
- Input validation using @Valid and custom business logic
- Proper error messages for debugging
- Exception handling via GlobalExceptionHandler

### 3. Security
- Bearer token authentication required
- Uses SecurityContextHolder to get current user
- Tracks who created the package (createdBy field)

### 4. Audit Trail
- createdAt, createdBy, updatedAt, updatedBy fields
- Soft delete support with deletedAt and deletedBy fields
- Comprehensive logging at INFO and DEBUG levels

### 5. API Standards
- Follows REST conventions with /api/v1/ prefix
- Uses standard HTTP methods (POST for creation)
- Consistent response format with ApiResponse wrapper
- Swagger/OpenAPI documentation with @Operation annotations

## Usage Example

### cURL Request:
```bash
curl -X POST http://localhost:8080/api/v1/course-packages/with-courses \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "packageName": "Advanced Learning",
    "description": "Complete advanced course collection",
    "monthlyPrice": 49.99,
    "yearlyPrice": 499.99,
    "childLimit": 10,
    "courseLimit": 50,
    "isActive": true,
    "courseIds": [1, 2, 3, 4, 5]
  }'
```

## Key Benefits

1. **Atomic Operations** - Package and courses assigned in single transaction
2. **Data Validation** - Comprehensive checks before assignment
3. **Audit Trail** - Track all changes with user information
4. **Error Handling** - Clear error messages for debugging
5. **Security** - Token-based authentication and user tracking
6. **Scalability** - Efficient bulk assignment of courses
7. **API Documentation** - Swagger annotations for easy discovery

## Related Endpoints

- `POST /api/v1/course-packages` - Create package without courses
- `GET /api/v1/course-packages` - Get all packages
- `GET /api/v1/course-packages/{id}` - Get package by ID
- `PUT /api/v1/course-packages/{id}` - Update package
- `DELETE /api/v1/course-packages/{id}` - Soft delete package
- `POST /api/v1/course-packages/{id}/restore` - Restore deleted package

