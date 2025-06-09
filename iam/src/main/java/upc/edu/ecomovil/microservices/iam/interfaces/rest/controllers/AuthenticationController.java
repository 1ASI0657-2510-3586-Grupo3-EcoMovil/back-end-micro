package upc.edu.ecomovil.microservices.iam.interfaces.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.ecomovil.microservices.iam.domain.services.UserCommandService;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.resources.AuthenticatedUserResource;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.resources.SignInResource;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.resources.SignUpResource;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.resources.UserResource;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import upc.edu.ecomovil.microservices.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

/**
 * AuthenticationController
 * <p>
 * This controller handles authentication-related requests such as sign-up and
 * sign-in.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Handles the sign-up request.
     * 
     * @param signUpResource the sign-up request resource
     * @return the created user resource
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Sign up a new user", description = "Sign up a new user with username, password, and roles")
    @SecurityRequirements // No authentication required for sign-up
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var result = userCommandService.handle(signUpCommand);

        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(result.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    /**
     * Handles the sign-in request.
     * 
     * @param signInResource the sign-in request resource
     * @return the authenticated user resource with token
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Sign in a user", description = "Authenticate a user and return an access token")
    @SecurityRequirements // No authentication required for sign-in
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var result = userCommandService.handle(signInCommand);

        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(result.get());
        return ResponseEntity.ok(authenticatedUserResource);
    }
}
