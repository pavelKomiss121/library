package ru.mentee.library.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ApiResponseTimeFilter extends OncePerRequestFilter {

  private final MeterRegistry meterRegistry;
  private Timer apiResponseTimer;

  @PostConstruct
  public void init() {
    this.apiResponseTimer =
        Timer.builder("api_response_duration_seconds")
            .description("Duration of API response")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      filterChain.doFilter(request, response);
    } finally {
      sample.stop(apiResponseTimer);
    }
  }
}
