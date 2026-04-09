package es.upm.api.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    @NotBlank
    public String to;
    @NotBlank
    public String subject;
    @NotBlank
    public String body;
}
