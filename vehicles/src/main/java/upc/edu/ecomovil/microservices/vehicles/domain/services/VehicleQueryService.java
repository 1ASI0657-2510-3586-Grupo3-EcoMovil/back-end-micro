package upc.edu.ecomovil.microservices.vehicles.domain.services;

import upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates.Vehicle;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetAllVehiclesByTypeQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetAllVehiclesQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetVehicleByIdQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetVehiclesByOwnerIdQuery;

import java.util.List;
import java.util.Optional;

public interface VehicleQueryService {
    List<Vehicle> handle(GetAllVehiclesQuery query);

    Optional<Vehicle> handle(GetVehicleByIdQuery query);

    List<Vehicle> handle(GetAllVehiclesByTypeQuery query);

    List<Vehicle> handle(GetVehiclesByOwnerIdQuery query);
}
