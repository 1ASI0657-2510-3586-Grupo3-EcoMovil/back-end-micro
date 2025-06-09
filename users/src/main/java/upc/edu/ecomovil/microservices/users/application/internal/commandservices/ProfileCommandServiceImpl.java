package upc.edu.ecomovil.microservices.users.application.internal.commandservices;

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
    private final ProfileRepository profileRepository;
    private final ExternalPlanService externalPlanService;

    public ProfileCommandServiceImpl(ProfileRepository profileRepository, ExternalPlanService externalPlanService) {
        this.profileRepository = profileRepository;
        this.externalPlanService = externalPlanService;
    }

    @Override
    public Optional<Profile> handle(CreateProfileCommand command) {
        var ruc = new RucNumber(command.rucNumber());
        profileRepository.findByRuc(ruc).ifPresent(
                profile -> {
                    throw new IllegalArgumentException("Profile with RUC " + command.rucNumber() + " already exists");
                });

        // Verificar que el plan existe (opcional - podrías hacer esto asíncrono)
        if (command.planId() != null) {
            var plan = externalPlanService.fetchPlanById(command.planId());
            if (plan.isEmpty()) {
                throw new IllegalArgumentException("El plan con el ID especificado no existe");
            }
        }

        // Crear el perfil usando solo el constructor con IDs
        var profile = new Profile(command);
        profileRepository.save(profile);
        return Optional.of(profile);
    }
}
