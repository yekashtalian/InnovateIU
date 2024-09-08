package org.example;

import static org.assertj.core.api.Assertions.*;
import static org.example.DocumentManager.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentManagerTest {
  private DocumentManager documentManager = new DocumentManager();

  @BeforeEach
  void cleanUp() {
    documentManager.documents.clear();
  }

  @Test
  void save() {
    var expectedDocument =
        Document.builder()
            .title("Test Title")
            .content("Test Content")
            .author(Author.builder().id("testId").name("Test AUthor").build())
            .created(Instant.now())
            .build();

    var actualDocument = documentManager.save(expectedDocument);

    assertThat(actualDocument).isEqualTo(expectedDocument);
    assertThat(documentManager.documents.contains(expectedDocument)).isTrue();
  }

  @Test
  void saveShouldThrowExceptionIfDocumentIdAlreadyExists() {
    var document1 =
        Document.builder()
            .id("Test Id")
            .title("Test Title")
            .content("Test Content")
            .author(Author.builder().id("testId").name("Test AUthor").build())
            .created(Instant.now())
            .build();

    var document2 =
        Document.builder()
            .id("Test Id")
            .title("Test Title")
            .content("Test Content")
            .author(Author.builder().id("testId").name("Test AUthor").build())
            .created(Instant.now())
            .build();

    documentManager.save(document1);
    var exception = assertThrows(RuntimeException.class, () -> documentManager.save(document2));

    assertThat(exception.getMessage())
        .isEqualTo("Cannot add document, this document already exists!");
  }

  @Test
  void search() {
    var author = getAuthor("author2", "TestAuthor");
    var searchRequest =
        SearchRequest.builder()
            .titlePrefixes(List.of("Design"))
            .containsContents(List.of("patterns"))
            .authorIds(List.of("author2"))
            .createdFrom(Instant.parse("2023-03-01T10:00:00Z"))
            .createdTo(Instant.parse("2023-05-01T10:00:00Z"))
            .build();
    var savedDocument =
        documentManager.save(
            Document.builder()
                .title("Design Patterns in Java")
                .content("Learn design patterns in Java.")
                .author(author)
                .created(Instant.parse("2023-04-01T10:00:00Z"))
                .build());
    var expectedDocuments = List.of(savedDocument);

    var actualDocuments = documentManager.search(searchRequest);

    assertThat(actualDocuments).hasSize(1);
    assertThat(actualDocuments).isEqualTo(expectedDocuments);
  }

  @Test
  void searchByTitlePrefixes() {
    var author1 = getAuthor("author1", "Test Author1");
    var author2 = getAuthor("author2", "Test Author2");
    var searchRequest =
        SearchRequest.builder().titlePrefixes(List.of("Java", "Microservices with")).build();
    var savedDocument1 =
        documentManager.save(
            Document.builder()
                .title("Java Programming")
                .content("Learn Java step by step.")
                .author(author1)
                .created(Instant.parse("2023-01-01T10:00:00Z"))
                .build());
    var savedDocument2 =
        documentManager.save(
            Document.builder()
                .title("Microservices with Spring")
                .content("Guide to Microservices.")
                .author(author2)
                .created(Instant.parse("2023-03-01T10:00:00Z"))
                .build());
    var expectedDocuments = List.of(savedDocument1, savedDocument2);

    var actualDocuments = documentManager.search(searchRequest);

    assertThat(actualDocuments).hasSameSizeAs(actualDocuments);
    assertThat(actualDocuments).containsExactlyInAnyOrderElementsOf(expectedDocuments);
  }

  @Test
  void searchByContainsContents() {
    var author1 = getAuthor("author1", "Test Author1");
    var searchRequest =
        SearchRequest.builder().containsContents(List.of("Master advanced Java")).build();
    var document =
        documentManager.save(
            Document.builder()
                .title("Advanced Java Tips")
                .content("Master advanced Java techniques.")
                .author(author1)
                .created(Instant.parse("2023-02-01T10:00:00Z"))
                .build());
    var expectedDocuments = List.of(document);

    var actualDocuments = documentManager.search(searchRequest);

    assertThat(actualDocuments).hasSameSizeAs(expectedDocuments);
    assertThat(actualDocuments).containsExactlyInAnyOrderElementsOf(expectedDocuments);
  }

  @Test
  void searchByAuthorIds() {
    var author1 = getAuthor("author1", "Test Author1");
    var searchRequest = SearchRequest.builder().authorIds(List.of(author1.getId())).build();
    var savedDocument1 =
        documentManager.save(
            Document.builder()
                .title("Java Programming")
                .content("Learn Java step by step.")
                .author(author1)
                .created(Instant.parse("2023-01-01T10:00:00Z"))
                .build());
    var savedDocument2 =
        documentManager.save(
            Document.builder()
                .title("Advanced Java Tips")
                .content("Master advanced Java techniques.")
                .author(author1)
                .created(Instant.parse("2023-02-01T10:00:00Z"))
                .build());
    var expectedDocuments = List.of(savedDocument1, savedDocument2);

    var actualDocuments = documentManager.search(searchRequest);

    assertThat(actualDocuments).hasSameSizeAs(expectedDocuments);
    assertThat(actualDocuments).containsExactlyInAnyOrderElementsOf(expectedDocuments);
  }

  @Test
  void findById() {
    var author = getAuthor("author", "Test Author");
    var document =
        Document.builder()
            .title("Test Title")
            .content("Test Content")
            .author(author)
            .created(Instant.parse("2024-01-01T10:00:00Z"))
            .build();
    var savedDocument = documentManager.save(document);

    var maybeDocument = documentManager.findById(savedDocument.getId());

    assertThat(maybeDocument).isPresent();
    assertThat(maybeDocument.get().getId()).isEqualTo(savedDocument.getId());
  }

  private static Author getAuthor(String id, String name) {
    return Author.builder().id(id).name(name).build();
  }
}
