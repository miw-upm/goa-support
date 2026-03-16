package es.upm.api.domain.model;

import es.upm.api.infrastructure.jpa.entities.Status;
import es.upm.api.infrastructure.jpa.entities.Type;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class IssueDto {
    @Nullable
    public UUID id;
    @NotBlank(message = "Title is required")
    public String title;
    @NotBlank(message = "Description is required")
    public String description;
    @NotBlank(message = "Technical context is required")
    public String technicalContext;
    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    public Type type;
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    public Status status;
}
