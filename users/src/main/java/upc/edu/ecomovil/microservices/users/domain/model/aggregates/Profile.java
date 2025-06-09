package upc.edu.ecomovil.microservices.users.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import upc.edu.ecomovil.microservices.users.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import upc.edu.ecomovil.microservices.users.domain.model.commands.CreateProfileCommand;
import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.EmailAddress;
import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.PersonName;
import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.PhoneNumber;
import upc.edu.ecomovil.microservices.users.domain.model.valueobjects.RucNumber;

@Getter
@Entity
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Getter
    @Column(name = "plan_id")
    private Long planId;

    @Embedded
    private RucNumber ruc;

    @Embedded
    private PersonName name;

    @Embedded
    EmailAddress email;

    @Embedded
    private PhoneNumber phoneNumber;

    public Profile(Long userId, String firstName, String lastName, String email, String phoneNumber, String ruc) {
        this.userId = userId;
        this.name = new PersonName(firstName, lastName);
        this.email = new EmailAddress(email);
        this.phoneNumber = new PhoneNumber(phoneNumber);
        this.ruc = new RucNumber(ruc);
    }

    public Profile(CreateProfileCommand command) {
        this.userId = command.userId();
        this.name = new PersonName(command.firstName(), command.lastName());
        this.email = new EmailAddress(command.email());
        this.phoneNumber = new PhoneNumber(command.phoneNumber());
        this.ruc = new RucNumber(command.rucNumber());
        this.planId = command.planId();
    }

    public Profile() {
    }

    public void updateRuc(String ruc) {
        this.ruc = new RucNumber(ruc);
    }

    public String getRuc() {
        return ruc.getRucNumber();
    }

    public void updateName(String firstName, String lastName) {
        this.name = new PersonName(firstName, lastName);
    }

    public void updateEmail(String email) {
        this.email = new EmailAddress(email);
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = new PhoneNumber(phoneNumber);
    }

    public String getFullName() {
        return name.getFullName();
    }

    public String getEmail() {
        return email.address();
    }

    public String getPhoneNumber() {
        return phoneNumber.getPhoneNumber();
    }

    public void setPlan(Long planId) {
        this.planId = planId;
    }
}
