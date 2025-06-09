package upc.edu.ecomovil.microservices.iam.interfaces.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import upc.edu.ecomovil.microservices.iam.domain.model.queries.GetAllUsersQuery;
import upc.edu.ecomovil.microservices.iam.domain.model.queries.GetUserByIdQuery;
import upc.edu.ecomovil.microservices.iam.domain.model.queries.GetUserByUsernameQuery;
import upc.edu.ecomovil.microservices.iam.domain.services.UserQueryService;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.resources.UserResource;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

import java.util.List;

/**
 * UsersController
 * <p>
 * This controller handles user-related requests such as retrieving user
 * information.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User Management Endpoints")
public class UsersController {

    private final UserQueryService userQueryService;

    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    /**
     * Handles the get all users request.
     * 
     * @return the list of all users
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users in the system")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var getAllUsersQuery = new GetAllUsersQuery();
        var users = userQueryService.handle(getAllUsersQuery);
        var userResources = UserResourceFromEntityAssembler.toResourceFromEntityList(users);
        return ResponseEntity.ok(userResources);
    }

    /**
     * Handles the get user by ID request.
     * 
     * @param userId the user ID
     * @return the user resource
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by their ID")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }

    /**
     * Handles the get user by username request.
     * 
     * @param username the username
     * @return the user resource
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve a user by their username")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResource> getUserByUsername(@PathVariable String username) {
        var getUserByUsernameQuery = new GetUserByUsernameQuery(username);
        var user = userQueryService.handle(getUserByUsernameQuery);

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }
}
