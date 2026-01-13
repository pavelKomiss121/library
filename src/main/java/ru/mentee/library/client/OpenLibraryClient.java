package ru.mentee.library.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mentee.library.api.dto.OpenLibraryBookDto;

@FeignClient(name = "open-library-client", url = "${openlibrary.api.url}")
public interface OpenLibraryClient {
  @GetMapping("/api/books")
  Map<String, OpenLibraryBookDto> getBookByIsbn( // ← Изменить на Map
      @RequestParam("bibkeys") String bibkeys,
      @RequestParam("format") String format,
      @RequestParam("jscmd") String jscmd);
}
