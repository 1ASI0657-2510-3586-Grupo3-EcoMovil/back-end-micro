package upc.edu.ecomovil.microservices.iam.application.internal.commandservices;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.events.EventPublishingService;
import upc.edu.ecomovil.microservices.iam.application.internal.outboundservices.hashing.HashingService;
import upc.edu.ecomovil.microservices.iam.infrastructure.tokens.jwt.services.BearerTokenService;
import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.Role;
import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.User;
import upc.edu.ecomovil.microservices.iam.domain.model.commands.SignInCommand;
import upc.edu.ecomovil.microservices.iam.domain.model.commands.SignUpCommand;
import upc.edu.ecomovil.microservices.iam.domain.model.valueobjects.Roles;
import upc.edu.ecomovil.microservices.iam.domain.services.UserCommandService;
import upc.edu.ecomovil.microservices.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import upc.edu.ecomovil.microservices.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * User command service implementation.
 * <p>
 * This class implements the UserCommandService interface and provides
 * the implementation for the SignInCommand and SignUpCommand commands.
 * </p>
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final BearerTokenService tokenService;
    private final EventPublishingService eventPublishingService;

    public UserCommandServiceImpl(UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            BearerTokenService tokenService,
            EventPublishingService eventPublishingService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.eventPublishingService = eventPublishingService;
    }

    /**
     * Handle the sign-up command.
     * 
     * @param command the sign-up command containing username, password, and roles
     * @return an optional containing the created user
     * @throws RuntimeException if the username is already taken
     */
    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new RuntimeException("Username is already taken");
        }

        Set<Role> roles = new HashSet<>();
        if (command.roles() == null || command.roles().isEmpty()) {
            // Assign default role
            Role defaultRole = roleRepository.findByName(Roles.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            roles.add(defaultRole);
        } else {
            // Assign specified roles
            for (String roleName : command.roles()) {
                Role role = roleRepository.findByName(Roles.valueOf(roleName))
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            }
        }

        var hashedPassword = hashingService.encode(command.password());
        var user = new User(command.username(), hashedPassword, command.email(), roles);

        try {
            var savedUser = userRepository.save(user);

            // Publish user registered event
            eventPublishingService.publishUserRegisteredEvent(
                    savedUser.getId(),
                    savedUser.getUsername());

            return Optional.of(savedUser);
        } catch (Exception e) {
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    /**
     * Handle the sign-in command.
     * 
     * @param command the sign-in command containing username and password
     * @return an optional containing the user and the generated token
     * @throws RuntimeException if the credentials are invalid
     */
    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());

        if (user.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.get().getIsActive()) {
            throw new RuntimeException("User account is deactivated");
        }

        if (!hashingService.matches(command.password(), user.get().getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Update last login
        user.get().updateLastLogin();
        userRepository.save(user.get());

        // Extract roles from user
        java.util.List<String> roles = user.get().getRoles().stream()
                .map(role -> role.getName().name())
                .collect(java.util.stream.Collectors.toList());

        var token = tokenService.generateToken(user.get().getUsername(), roles, user.get().getId());

        // Publish user authenticated event
        eventPublishingService.publishUserAuthenticatedEvent(
                user.get().getId(),
                user.get().getUsername());

        return Optional.of(ImmutablePair.of(user.get(), token));
    }
}
