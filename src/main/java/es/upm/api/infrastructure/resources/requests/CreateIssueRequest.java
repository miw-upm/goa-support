package es.upm.api.infrastructure.resources.requests;

import es.upm.api.infrastructure.jpa.entities.Type;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateIssueRequest {
    @NotBlank(message = "Title is required")
    public String title;
    @NotBlank(message = "Description is required")
    public String description;
    @NotBlank(message = "Technical context is required")
    public String technicalContext;
    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    public Type type;
}
