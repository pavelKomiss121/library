package ru.mentee.library.api.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookInfoResponse {
  private String title;
  private List<String> authors;
  private List<String> publishers;
  private String publishDate;
  private Integer numberOfPages;
  private String isbn10;
  private String isbn13;

  public static BookInfoResponse from(OpenLibraryBookDto dto) {
    if (dto == null) {
      return null;
    }

    return BookInfoResponse.builder()
        .title(dto.getTitle())
        .authors(
            dto.getAuthors() != null
                ? dto.getAuthors().stream()
                    .map(OpenLibraryBookDto.Author::getName)
                    .collect(Collectors.toList())
                : List.of())
        .publishers(
            dto.getPublishers() != null
                ? dto.getPublishers().stream()
                    .map(OpenLibraryBookDto.Publisher::getName)
                    .collect(Collectors.toList())
                : List.of())
        .publishDate(dto.getPublishDate())
        .numberOfPages(dto.getNumberOfPages())
        .isbn10(
            dto.getIdentifiers() != null && dto.getIdentifiers().getIsbn10() != null
                ? dto.getIdentifiers().getIsbn10().get(0)
                : null)
        .isbn13(
            dto.getIdentifiers() != null && dto.getIdentifiers().getIsbn13() != null
                ? dto.getIdentifiers().getIsbn13().get(0)
                : null)
        .build();
  }
}
