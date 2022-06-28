package com.tariqsiddiqui.silentworlds.repositories;

import com.tariqsiddiqui.silentworlds.models.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepo extends JpaRepository<Poll, Long> {
}
