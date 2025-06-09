package upc.edu.ecomovil.microservices.iam.infrastructure.persistence.jpa.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import upc.edu.ecomovil.microservices.iam.domain.model.aggregates.Role;
import upc.edu.ecomovil.microservices.iam.domain.model.valueobjects.Roles;
import upc.edu.ecomovil.microservices.iam.infrastructure.persistence.jpa.repositories.RoleRepository;

/**
 * Data loader for IAM service.
 * <p>
 * This component initializes the database with default roles when the
 * application starts.
 * It ensures that the basic roles (USER, ADMIN, MODERATOR) exist in the
 * database.
 * </p>
 */
@Component
public class IamDataLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(IamDataLoader.class);

    private final RoleRepository roleRepository;

    public IamDataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Initializing IAM service data...");

        // Create default roles if they don't exist
        createRoleIfNotExists(Roles.ROLE_USER);
        createRoleIfNotExists(Roles.ROLE_ADMIN);
        createRoleIfNotExists(Roles.ROLE_MODERATOR);

        logger.info("IAM service data initialization completed.");
    }

    private void createRoleIfNotExists(Roles roleEnum) {
        if (roleRepository.findByName(roleEnum).isEmpty()) {
            Role role = new Role(roleEnum);
            roleRepository.save(role);
            logger.info("Created role: {}", roleEnum.name());
        } else {
            logger.debug("Role {} already exists", roleEnum.name());
        }
    }
}
