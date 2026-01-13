package ru.mentee.library.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryBookDto {
  @JsonProperty("title")
  private String title;

  @JsonProperty("authors")
  private List<Author> authors;

  @JsonProperty("publishers")
  private List<Publisher> publishers;

  @JsonProperty("publish_date")
  private String publishDate;

  @JsonProperty("number_of_pages")
  private Integer numberOfPages;

  @JsonProperty("identifiers")
  private Identifiers identifiers;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Author {
    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Publisher {
    @JsonProperty("name")
    private String name;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Identifiers {
    @JsonProperty("isbn_10")
    private List<String> isbn10;

    @JsonProperty("isbn_13")
    private List<String> isbn13;
  }
}
