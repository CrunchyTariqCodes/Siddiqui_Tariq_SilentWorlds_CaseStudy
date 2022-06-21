package com.silentworlds.siddiqui_tariq_slientworlds_casestudy.repositories;

import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.Bookshelf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookshelfRepo extends JpaRepository<Bookshelf, Long> {
    List<Bookshelf> findAllByUserId(long id);
    Bookshelf findBookshelfByUserIdAndBookId(long userId, long bookId);
}
