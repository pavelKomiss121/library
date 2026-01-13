package ru.mentee.library.api.dto;

import lombok.Data;

@Data
public class CreateBookRequest {
    private String title;
    private String author;
    private Integer publicationYear;
}



