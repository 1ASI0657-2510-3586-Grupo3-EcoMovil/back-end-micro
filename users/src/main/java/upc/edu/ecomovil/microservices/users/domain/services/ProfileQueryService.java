package upc.edu.ecomovil.microservices.users.domain.services;

import upc.edu.ecomovil.microservices.users.domain.model.aggregates.Profile;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetAllProfilesQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfileByEmailQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfileByIdQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfileByRucQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfileByUserIdQuery;
import upc.edu.ecomovil.microservices.users.domain.model.queries.GetProfilesByPlanIdQuery;

import java.util.List;
import java.util.Optional;

public interface ProfileQueryService {
    Optional<Profile> handle(GetProfileByIdQuery query);

    List<Profile> handle(GetAllProfilesQuery query);

    Optional<Profile> handle(GetProfileByEmailQuery query);

    Optional<Profile> handle(GetProfileByRucQuery query);

    Optional<Profile> handle(GetProfileByUserIdQuery query);

    List<Profile> handle(GetProfilesByPlanIdQuery query);
}
