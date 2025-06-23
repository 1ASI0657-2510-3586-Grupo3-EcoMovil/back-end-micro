package upc.edu.ecomovil.microservices.users.application.internal.queryservices;

import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.users.domain.model.aggregates.Profile;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetAllProfilesQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfileByEmailQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfileByIdQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfileByRucQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfileByUserIdQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfilesByPlanIdQuery;
import upc.edu.ecomovil.microservices.users.domain.services.ProfileQueryService;
import upc.edu.ecomovil.microservices.users.infrastructure.persistence.jpa.repositories.ProfileRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileQueryServiceImpl implements ProfileQueryService {
  private final ProfileRepository profileRepository;

  public ProfileQueryServiceImpl(ProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  @Override
  public Optional<Profile> handle(GetProfileByIdQuery query) {
    return profileRepository.findById(query.profileId());
  }

  @Override
  public List<Profile> handle(GetAllProfilesQuery query) {
    return profileRepository.findAll();
  }

  @Override
  public Optional<Profile> handle(GetProfileByEmailQuery query) {
    return profileRepository.findByEmail(query.email());
  }

  @Override
  public Optional<Profile> handle(GetProfileByRucQuery query) {
    return profileRepository.findByRuc(query.ruc());
  }

  @Override
  public Optional<Profile> handle(GetProfileByUserIdQuery query) {
    return profileRepository.findByUserId(query.userId());
  }

  @Override
  public List<Profile> handle(GetProfilesByPlanIdQuery query) {
    return profileRepository.findByPlanId(query.planId());
  }
}
