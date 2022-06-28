package com.tariqsiddiqui.silentworlds.repositories;

import com.tariqsiddiqui.silentworlds.models.Comment;
import com.tariqsiddiqui.silentworlds.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPost(Post post);
}
