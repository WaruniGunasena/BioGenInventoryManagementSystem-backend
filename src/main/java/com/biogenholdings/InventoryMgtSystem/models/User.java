package com.biogenholdings.InventoryMgtSystem.models;

import com.biogenholdings.InventoryMgtSystem.enums.UserRole;
import com.biogenholdings.InventoryMgtSystem.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Data
@Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Column(unique = true)
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private Boolean isTempPassword;

    //@NotBlank(message = "PhoneNumber is required")
    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Column(name = "nic_number", nullable = true)
    private String nicNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String province;

    @Column(name = "postal_code")
    private String postalCode;

    private String address;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Column(name = "hiring_date")
    private LocalDate hiringDate;

    @Lob
    @Column(name = "nic_copy")
    private byte[] nicPicture;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='******' " +
                ", isTempPassword=" + isTempPassword +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", nicNumber='" + nicNumber + '\'' +
                ", role=" + role +
                ", province='" + province + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", address='" + address + '\'' +
                ", userStatus=" + userStatus +
                ", hiringDate=" + hiringDate +
                ", nicPicture=" + Arrays.toString(nicPicture) +
                ", createdAt=" + createdAt +
                '}';
    }
}
