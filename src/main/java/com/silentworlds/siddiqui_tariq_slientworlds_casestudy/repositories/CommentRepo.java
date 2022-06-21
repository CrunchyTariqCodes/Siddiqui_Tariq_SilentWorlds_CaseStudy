package com.silentworlds.siddiqui_tariq_slientworlds_casestudy.repositories;

import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.Comment;
import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPost(Post post);
}
