package com.tariqsiddiqui.silentworlds.repositories;

import com.tariqsiddiqui.silentworlds.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepo extends JpaRepository<Genre, Long> {
    List<Genre> findByNameIsLike(String name);
}
