package ru.mentee.library.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryResponse {
  private Map<String, OpenLibraryBookDto> data;

  public OpenLibraryBookDto getFirstBook(){
    if (data == null || data.isEmpty()) {
      return null;
    }
    return data.values().iterator().next();
  }
}
