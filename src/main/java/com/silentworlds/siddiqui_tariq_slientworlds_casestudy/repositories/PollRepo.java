package com.silentworlds.siddiqui_tariq_slientworlds_casestudy.repositories;

import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepo extends JpaRepository<Poll, Long> {
}
