package ru.mentee.library.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mentee.library.domain.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}



