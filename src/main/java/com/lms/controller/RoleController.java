package com.lms.controller;

import com.lms.dto.response.ApiResponse;
import com.lms.entity.Role;
import com.lms.entity.Roles;
import com.lms.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for role management endpoints.
 * Handles CRUD operations for roles with role-based access control.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Role management APIs")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    /**
     * Get all roles
     * 
     * @return ResponseEntity with list of all roles
     */
    @Operation(
            summary = "Get all roles",
            description = "Retrieve all available roles in the system"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Roles retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Roles>>> getAllRoles() {
        log.info("Fetching all roles");
        
        List<Roles> roles = roleService.getAllRoles();
        
        return ResponseEntity.ok(
                ApiResponse.success("Roles retrieved successfully", roles)
        );
    }

    /**
     * Get role by ID
     * 
     * @param id the role ID
     * @return ResponseEntity with the role
     */
    @Operation(
            summary = "Get role by ID",
            description = "Retrieve a specific role by its ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Roles>> getRoleById(@PathVariable Long id) {
        log.info("Fetching role by ID: {}", id);
        
        return roleService.getRoleById(id)
                .map(role -> ResponseEntity.ok(
                        ApiResponse.success("Role retrieved successfully", role)
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Role not found"))
                );
    }

    /**
     * Get role by name
     * 
     * @param name the role name
     * @return ResponseEntity with the role
     */
    @Operation(
            summary = "Get role by name",
            description = "Retrieve a specific role by its name"
    )
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<Roles>> getRoleByName(@PathVariable String name) {
        log.info("Fetching role by name: {}", name);
        
        return roleService.getRoleByName(name)
                .map(role -> ResponseEntity.ok(
                        ApiResponse.success("Role retrieved successfully", role)
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Role not found"))
                );
    }

    /**
     * Create a new role (Admin only)
     * 
     * @param role the role to create
     * @return ResponseEntity with the created role
     */
    @Operation(
            summary = "Create a new role",
            description = "Create a new role in the system (Admin only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Role created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or role already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Roles>> createRole(@RequestBody Roles role) {
        log.info("Creating new role: {}", role.getRole().name());
        
        try {
            Roles createdRole = roleService.createRole(role);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Role created successfully", createdRole));
        } catch (IllegalArgumentException ex) {
            log.warn("Role creation failed: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Update an existing role (Admin only)
     * 
     * @param id the role ID
     * @param role the updated role data
     * @return ResponseEntity with the updated role
     */
    @Operation(
            summary = "Update a role",
            description = "Update an existing role in the system (Admin only)"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Roles>> updateRole(
            @PathVariable Long id,
            @RequestBody Roles role) {
        log.info("Updating role: {}", id);
        
        try {
            Roles updatedRole = roleService.updateRole(id, role);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Role updated successfully", updatedRole)
            );
        } catch (IllegalArgumentException ex) {
            log.warn("Role update failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Delete a role (Admin only)
     * 
     * @param id the role ID to delete
     * @return ResponseEntity with success message
     */
    @Operation(
            summary = "Delete a role",
            description = "Delete a role from the system (Admin only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Role not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable Long id) {
        log.info("Deleting role: {}", id);
        
        try {
            roleService.deleteRole(id);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Role deleted successfully", "Role deleted")
            );
        } catch (IllegalArgumentException ex) {
            log.warn("Role deletion failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }
}

