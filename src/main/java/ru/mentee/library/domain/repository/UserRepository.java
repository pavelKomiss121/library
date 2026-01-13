package ru.mentee.library.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mentee.library.domain.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}



