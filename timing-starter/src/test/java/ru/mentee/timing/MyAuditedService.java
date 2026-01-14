package ru.mentee.timing;

import org.springframework.stereotype.Service;

@Service
public class MyAuditedService {

    @Auditable(action = "DO_SOMETHING")
    public void doSomething(String data) {
        // Метод для тестирования
    }
}

