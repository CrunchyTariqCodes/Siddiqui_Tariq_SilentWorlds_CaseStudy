package com.tariqsiddiqui.silentworlds.repositories;

import com.tariqsiddiqui.silentworlds.models.Club;
import com.tariqsiddiqui.silentworlds.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepo extends JpaRepository<Post, Long> {

    List<Post> findAllByClub(Club club);
    List<Post> findAllByClubOrderByIdDesc(Club club);
}
