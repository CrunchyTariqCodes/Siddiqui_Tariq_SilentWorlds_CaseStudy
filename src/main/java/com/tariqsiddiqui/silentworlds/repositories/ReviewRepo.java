package com.tariqsiddiqui.silentworlds.repositories;

import com.tariqsiddiqui.silentworlds.models.Review;
import com.tariqsiddiqui.silentworlds.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Long> {

    Review findByOwnerIdAndBookId(long ownerId, long bookId);
    List<Review> findAllByOwner(User owner);
    List<Review> findAllByBookId(long bookId);
}
