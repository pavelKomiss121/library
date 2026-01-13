package ru.mentee.library.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookRequest {
    @NotBlank(message = "Title is required")
    @NotNull(message = "Title cannot be null")
    private String title;
    
    @NotBlank(message = "Author is required")
    @NotNull(message = "Author cannot be null")
    private String author;
    
    @Min(value = 0, message = "Publication year must be non-negative")
    private Integer publicationYear;
}



