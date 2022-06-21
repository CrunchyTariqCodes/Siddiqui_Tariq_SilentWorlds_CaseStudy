package com.silentworlds.siddiqui_tariq_slientworlds_casestudy.repositories;

import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.Review;
import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Long> {

    Review findByOwnerIdAndBookId(long ownerId, long bookId);
    List<Review> findAllByOwner(User owner);
    List<Review> findAllByBookId(long bookId);
}
