package com.example.fastcampusbookstore;

import com.example.fastcampusbookstore.entity.Book;
import com.example.fastcampusbookstore.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @Test
    void saveAndFindBook() {
        Book book = new Book();
        book.setBookName("포트폴리오 테스트 도서");
        book.setAuthor("홍길동");
        book.setPrice(java.math.BigDecimal.valueOf(15000));
        bookRepository.save(book);

        Optional<Book> found = bookRepository.findById(book.getBookId());
        assertThat(found).isPresent();
        assertThat(found.get().getBookName()).isEqualTo("포트폴리오 테스트 도서");
    }
} 