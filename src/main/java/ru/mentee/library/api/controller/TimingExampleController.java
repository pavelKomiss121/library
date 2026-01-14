package ru.mentee.library.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.timing.TimingService;

@RestController
@RequestMapping("/api/timing-example")
public class TimingExampleController {

  private final TimingService timingService;

  public TimingExampleController(TimingService timingService) {
    this.timingService = timingService;
  }

  @GetMapping("/test")
  public String testMethod() {
    long startTime = System.currentTimeMillis();

    // Имитация работы метода
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    long executionTime = System.currentTimeMillis() - startTime;
    timingService.logExecutionTime("testMethod", executionTime);

    return "Method executed in " + executionTime + " ms";
  }
}
