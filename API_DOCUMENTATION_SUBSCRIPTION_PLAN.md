# SubscriptionPlan CRUD Operations API Documentation

## Overview
Complete CRUD (Create, Read, Update, Delete) operations for the SubscriptionPlan entity with full support for soft deletes, audit trails, and role-based access control.

## Files Created

### 1. Repository: SubscriptionPlanRepository
**Location:** `/home/suraj/Documents/LMS/src/main/java/com/lms/repository/SubscriptionPlanRepository.java`

Provides database operations with custom query methods:
- `findByName(String name)` - Find plan by name
- `findAllActive()` - Find all non-deleted plans
- `findByIsDeletedFalse()` - Find all active plans
- `existsByName(String name)` - Check plan existence
- `searchPlans(String keyword)` - Search by name or description

### 2. DTOs

#### CreateSubscriptionPlanRequest
**Location:** `/home/suraj/Documents/LMS/src/main/java/com/lms/dto/request/CreateSubscriptionPlanRequest.java`

Fields:
- `name` (required) - Plan name
- `description` (required) - Plan description
- `monthlyPrice` (required) - Monthly subscription price
- `yearlyPrice` (required) - Yearly subscription price
- `userLimit` (required) - Maximum users allowed
- `childLimit` (required) - Maximum children allowed
- `coursePackageId` (required) - Associated course package ID

#### UpdateSubscriptionPlanRequest
**Location:** `/home/suraj/Documents/LMS/src/main/java/com/lms/dto/request/UpdateSubscriptionPlanRequest.java`

All fields are optional for partial updates

#### SubscriptionPlanResponse
**Location:** `/home/suraj/Documents/LMS/src/main/java/com/lms/dto/response/SubscriptionPlanResponse.java`

Response fields include:
- All plan details (name, description, pricing, limits)
- Course package information
- Audit fields (createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy)
- isDeleted flag for soft delete tracking

### 3. Service: SubscriptionPlanService
**Location:** `/home/suraj/Documents/LMS/src/main/java/com/lms/service/SubscriptionPlanService.java`

**Key Methods:**
- `createSubscriptionPlan(CreateSubscriptionPlanRequest)` - Create new plan
- `getAllSubscriptionPlans()` - Get all active plans
- `getSubscriptionPlanById(Long id)` - Get plan by ID
- `searchSubscriptionPlans(String keyword)` - Search plans
- `updateSubscriptionPlan(Long id, UpdateSubscriptionPlanRequest)` - Update plan
- `deleteSubscriptionPlan(Long id)` - Soft delete plan
- `restoreSubscriptionPlan(Long id)` - Restore deleted plan
- `planExists(String name)` - Check plan existence

**Features:**
- Comprehensive validation (duplicate names, course package existence)
- Soft delete with audit trail
- Security context integration for user tracking
- Full transaction support (@Transactional)
- Detailed logging for debugging

### 4. Controller: SubscriptionPlanController
**Location:** `/home/suraj/Documents/LMS/src/main/java/com/lms/controller/SubscriptionPlanController.java`

## API Endpoints

### 1. Create Subscription Plan
```
POST /api/v1/subscription-plans
```

**Request Body:**
```json
{
  "name": "Premium Plan",
  "description": "Full access plan with all courses",
  "monthlyPrice": 49.99,
  "yearlyPrice": 499.99,
  "userLimit": 10,
  "childLimit": 20,
  "coursePackageId": 1
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Subscription plan created successfully",
  "data": {
    "id": 1,
    "name": "Premium Plan",
    "description": "Full access plan with all courses",
    "monthlyPrice": 49.99,
    "yearlyPrice": 499.99,
    "userLimit": 10,
    "childLimit": 20,
    "coursePackageId": 1,
    "coursePackageName": "Complete Package",
    "createdAt": "2025-12-11T10:30:00",
    "createdBy": "admin",
    "updatedAt": null,
    "updatedBy": null,
    "deletedAt": null,
    "deletedBy": null,
    "isDeleted": false
  },
  "timestamp": "2025-12-11T10:30:00"
}
```

**Error Responses:**
- `400 Bad Request` - Duplicate plan name or invalid course package
- `404 Not Found` - Course package not found
- `401 Unauthorized` - Authentication required

### 2. Get All Subscription Plans
```
GET /api/v1/subscription-plans
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Subscription plans retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Premium Plan",
      "description": "Full access plan",
      ...
    },
    {
      "id": 2,
      "name": "Basic Plan",
      "description": "Limited access plan",
      ...
    }
  ],
  "timestamp": "2025-12-11T10:30:00"
}
```

### 3. Get Subscription Plan by ID
```
GET /api/v1/subscription-plans/{id}
```

**Parameters:**
- `id` (path parameter) - Subscription plan ID

**Response (200 OK):**
Returns single subscription plan details

**Error Responses:**
- `404 Not Found` - Plan not found or deleted
- `401 Unauthorized` - Authentication required

### 4. Search Subscription Plans
```
GET /api/v1/subscription-plans/search?keyword=premium
```

**Parameters:**
- `keyword` (query parameter) - Search keyword (searches in name and description)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Subscription plans retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Premium Plan",
      ...
    }
  ],
  "timestamp": "2025-12-11T10:30:00"
}
```

### 5. Update Subscription Plan
```
PUT /api/v1/subscription-plans/{id}
```

**Request Body (partial update):**
```json
{
  "monthlyPrice": 59.99,
  "yearlyPrice": 599.99,
  "userLimit": 15
}
```

**Response (200 OK):**
Returns updated subscription plan

**Error Responses:**
- `404 Not Found` - Plan not found
- `400 Bad Request` - Invalid data or duplicate name
- `401 Unauthorized` - Authentication required

### 6. Delete Subscription Plan (Soft Delete)
```
DELETE /api/v1/subscription-plans/{id}
```

**Parameters:**
- `id` (path parameter) - Subscription plan ID

**Response (204 No Content):**
No content returned on successful deletion

**Error Responses:**
- `404 Not Found` - Plan not found
- `401 Unauthorized` - Authentication required

### 7. Restore Subscription Plan
```
POST /api/v1/subscription-plans/{id}/restore
```

**Parameters:**
- `id` (path parameter) - Subscription plan ID to restore

**Response (200 OK):**
Returns restored subscription plan

**Error Responses:**
- `404 Not Found` - Plan not found
- `400 Bad Request` - Plan is not deleted
- `401 Unauthorized` - Authentication required

## Key Features

### 1. Soft Delete
- Plans are marked as deleted without removing from database
- `isDeleted` flag tracks deletion status
- `deletedAt` timestamp records when deletion occurred
- `deletedBy` field tracks who deleted the plan
- Restore functionality to recover deleted plans

### 2. Audit Trail
- `createdAt` - Automatically set on creation
- `createdBy` - Tracks user who created the plan
- `updatedAt` - Automatically updated on modification
- `updatedBy` - Tracks user who updated the plan
- All timestamps use LocalDateTime

### 3. Security & Authentication
- All endpoints require Bearer token authentication
- Uses SecurityContextHolder to capture current user
- User information automatically tracked in audit fields
- Role-based access control support

### 4. Data Validation
- Name uniqueness validation
- Course package existence verification
- Deleted course package prevention
- Required field validation
- Partial update support with null checks

### 5. Transaction Support
- All operations use @Transactional annotation
- Atomic operations ensure data consistency
- Read-only transactions for GET operations
- Full rollback on validation failures

### 6. Error Handling
- Global exception handler integration
- ResourceNotFoundException for missing entities
- IllegalArgumentException for business rule violations
- Meaningful error messages for debugging
- Proper HTTP status codes

## cURL Examples

### Create Plan
```bash
curl -X POST http://localhost:8080/api/v1/subscription-plans \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Premium Plan",
    "description": "Full access plan",
    "monthlyPrice": 49.99,
    "yearlyPrice": 499.99,
    "userLimit": 10,
    "childLimit": 20,
    "coursePackageId": 1
  }'
```

### Get All Plans
```bash
curl -X GET http://localhost:8080/api/v1/subscription-plans \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get Plan by ID
```bash
curl -X GET http://localhost:8080/api/v1/subscription-plans/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Search Plans
```bash
curl -X GET "http://localhost:8080/api/v1/subscription-plans/search?keyword=premium" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Update Plan
```bash
curl -X PUT http://localhost:8080/api/v1/subscription-plans/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "monthlyPrice": 59.99,
    "yearlyPrice": 599.99
  }'
```

### Delete Plan
```bash
curl -X DELETE http://localhost:8080/api/v1/subscription-plans/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Restore Plan
```bash
curl -X POST http://localhost:8080/api/v1/subscription-plans/1/restore \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Business Logic

### Plan Creation
1. Validates unique plan name
2. Verifies course package exists and is not deleted
3. Sets audit fields (createdBy, isDeleted)
4. Persists to database
5. Returns created plan details

### Plan Update
1. Validates plan exists and is not deleted
2. Checks name uniqueness (if name is updated)
3. Verifies course package if changed
4. Updates only provided fields (partial update support)
5. Sets updatedBy field
6. Persists changes

### Plan Deletion (Soft Delete)
1. Validates plan exists and is not already deleted
2. Marks isDeleted as true
3. Sets deletedAt timestamp
4. Sets deletedBy field
5. Persists soft delete markers

### Plan Restoration
1. Validates plan exists and is deleted
2. Marks isDeleted as false
3. Clears deletedAt timestamp
4. Clears deletedBy field
5. Sets updatedBy field
6. Persists restoration

## Standards & Best Practices

✅ RESTful API design with /api/v1/ prefix
✅ Standard HTTP methods (GET, POST, PUT, DELETE)
✅ Consistent response format with ApiResponse wrapper
✅ Comprehensive validation and error handling
✅ Security with Bearer token authentication
✅ Audit trail with user tracking
✅ Soft delete for data preservation
✅ Transaction management
✅ Swagger/OpenAPI documentation
✅ Detailed logging for debugging
✅ Meaningful error messages

## Related Endpoints

- Course Management: `/api/v1/courses`
- Course Packages: `/api/v1/course-packages`
- User Management: `/api/v1/users`
- Role Management: `/api/v1/roles`

