package upc.edu.ecomovil.microservices.vehicles.application.internal.queryservices;

import org.springframework.stereotype.Service;
import upc.edu.ecomovil.microservices.vehicles.domain.model.aggregates.Vehicle;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetAllVehiclesByTypeQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetAllVehiclesQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetVehicleByIdQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.model.queries.GetVehiclesByOwnerIdQuery;
import upc.edu.ecomovil.microservices.vehicles.domain.services.VehicleQueryService;
import upc.edu.ecomovil.microservices.vehicles.infrastructure.persistence.jpa.repositories.VehicleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleQueryServiceImpl implements VehicleQueryService {

    private final VehicleRepository vehicleRepository;

    public VehicleQueryServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public List<Vehicle> handle(GetAllVehiclesQuery query) {
        return vehicleRepository.findAll();
    }

    @Override
    public Optional<Vehicle> handle(GetVehicleByIdQuery query) {
        return vehicleRepository.findById(query.vehicleId());
    }

    @Override
    public List<Vehicle> handle(GetAllVehiclesByTypeQuery query) {
        return vehicleRepository.findAllByDetailsType(query.type());
    }

    @Override
    public List<Vehicle> handle(GetVehiclesByOwnerIdQuery query) {
        return vehicleRepository.findAllByOwnerId(query.ownerId());
    }
}
