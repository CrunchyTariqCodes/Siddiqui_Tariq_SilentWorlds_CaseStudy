package com.tariqsiddiqui.silentworlds.repositories;

import com.tariqsiddiqui.silentworlds.models.Club;
import com.tariqsiddiqui.silentworlds.models.ClubMember;
import com.tariqsiddiqui.silentworlds.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubMemberRepo extends JpaRepository<ClubMember, Long> {
    ClubMember findClubMemberByUserAndClub(User user, Club club);
    List<ClubMember> findAllByClub(Club club);
}
