package com.silentworlds.siddiqui_tariq_slientworlds_casestudy.repositories;

import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.Club;
import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.ClubMember;
import com.silentworlds.siddiqui_tariq_slientworlds_casestudy.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubMemberRepo extends JpaRepository<ClubMember, Long> {
    ClubMember findClubMemberByUserAndClub(User user, Club club);
    List<ClubMember> findAllByClub(Club club);
}
