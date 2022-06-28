package com.tariqsiddiqui.silentworlds.repositories;

import com.tariqsiddiqui.silentworlds.models.Club;
import com.tariqsiddiqui.silentworlds.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubRepo extends JpaRepository<Club, Long> {
    List<Club> findByNameIsLike(String query);
    List<Club> findByGenreIdAndNameIsLike(long id, String query);
    List<Club> findAllByOwnerId(long id);
    List<Club> findAllByOwner(User user);
    Club findByName(String name);
}
