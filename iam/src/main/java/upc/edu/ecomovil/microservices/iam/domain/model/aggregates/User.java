package upc.edu.ecomovil.microservices.iam.domain.model.aggregates;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User aggregate root.
 * <p>
 * This class represents a user in the system.
 * It contains the user's username, password, and roles.
 * </p>
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(max = 120)
    private String password;

    @NotBlank
    @Size(max = 120)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(String username, String password, String email, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        addRoles(roles);
    }

    /**
     * Add a role to the user.
     * 
     * @param role the role to add
     * @return the user instance
     */
    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    /**
     * Add roles to the user.
     * 
     * @param roles the roles to add
     * @return the user instance
     */
    public User addRoles(Set<Role> roles) {
        var validatedRoleSet = Role.validateRoleSet(List.copyOf(roles));
        this.roles.addAll(validatedRoleSet);
        return this;
    }

    /**
     * Update the last login timestamp.
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    /**
     * Activate the user account.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate the user account.
     */
    public void deactivate() {
        this.isActive = false;
    }
}
