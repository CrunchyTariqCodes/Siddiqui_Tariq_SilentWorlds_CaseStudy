package com.silentworlds.siddiqui_tariq_slientworlds_casestudy.repositories;

import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepo extends JpaRepository<Book, Long> {
    Book findBookById(long id);
    Book findBookByIsbnEquals(String isbn);
    Book findBookByGbreferenceEquals(String gbreference);
}
