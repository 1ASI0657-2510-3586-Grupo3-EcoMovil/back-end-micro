package upc.edu.ecomovil.microservices.users.application.internal.commandservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.users.application.internal.outboundservices.acl.ExternalPlanService;
import upc.edu.ecomovil.microservices.users.domain.model.aggregates.Profile;
import upc.edu.ecomovil.microservices.users.domain.model.commands.CreateProfileCommand;
import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.EmailAddress;
import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.RucNumber;
import upc.edu.ecomovil.microservices.users.domain.services.ProfileCommandService;
import upc.edu.ecomovil.microservices.users.infrastructure.persistence.jpa.repositories.ProfileRepository;

import java.util.Optional;

@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ProfileCommandServiceImpl.class);

    private final ProfileRepository profileRepository;
    private final ExternalPlanService externalPlanService;

    public ProfileCommandServiceImpl(ProfileRepository profileRepository, ExternalPlanService externalPlanService) {
        this.profileRepository = profileRepository;
        this.externalPlanService = externalPlanService;
    }

    @Override
    public Optional<Profile> handle(CreateProfileCommand command) {
        logger.info("Creating profile for userId: {} with planId: {}", command.userId(), command.planId());

        var ruc = new RucNumber(command.rucNumber());
        profileRepository.findByRuc(ruc).ifPresent(
                profile -> {
                    logger.error("Profile creation failed - RUC {} already exists", command.rucNumber());
                    throw new IllegalArgumentException("Profile with RUC " + command.rucNumber() + " already exists");
                });

        // Verificar que el plan existe
        if (command.planId() != null) {
            logger.info("Validating plan with ID: {}", command.planId());
            var plan = externalPlanService.fetchPlanById(command.planId());
            if (plan.isEmpty()) {
                logger.error("Plan validation failed - Plan with ID {} does not exist", command.planId());
                throw new IllegalArgumentException("El plan con el ID " + command.planId() + " no existe");
            }
            logger.info("Plan validation successful for ID: {}", command.planId());
        } else {
            logger.info("No planId provided, skipping plan validation");
        }

        // Crear el perfil usando solo el constructor con IDs
        var profile = new Profile(command);
        profileRepository.save(profile);
        logger.info("Profile created successfully with ID: {}", profile.getId());
        return Optional.of(profile);
    }
}
