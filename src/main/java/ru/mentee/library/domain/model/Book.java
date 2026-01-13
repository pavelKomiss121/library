package ru.mentee.library.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column(nullable = true)
  private Integer publicationYear; // Оставляем publicationYear

  @Column(nullable = true, unique = true) // ISBN должен быть уникальным
  private String isbn; // ← Добавить это поле

  @Column(nullable = true)
  @Builder.Default
  private Boolean available = true;
}
