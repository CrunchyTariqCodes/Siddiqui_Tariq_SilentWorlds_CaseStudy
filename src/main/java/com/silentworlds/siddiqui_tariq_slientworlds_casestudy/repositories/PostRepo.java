package com.silentworlds.siddiqui_tariq_slientworlds_casestudy.repositories;

import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.Club;
import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepo extends JpaRepository<Post, Long> {

    List<Post> findAllByClub(Club club);
    List<Post> findAllByClubOrderByIdDesc(Club club);
}
