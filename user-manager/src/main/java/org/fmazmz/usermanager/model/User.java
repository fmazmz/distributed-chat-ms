package org.fmazmz.usermanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    @Size(min = 3, max = 55)
    private String userName;

    @Column(nullable = false, unique = true)
    @Size(max = 255)
    @Email
    private String email;
}
