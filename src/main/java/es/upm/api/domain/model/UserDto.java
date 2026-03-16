package es.upm.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String mobile;
    private String email;
    private String firstName;
    private String familyName;
    private String address;
    private String city;
    private String postalCode;
    private String province;
    private String documentType;
    private String identity;
    private String role;
}
