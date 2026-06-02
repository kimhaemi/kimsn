package kr.or.kimsn.radar.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radar.data.dto.MembersDto;
import org.springframework.stereotype.Repository;

@Repository
public interface MembersRepository extends JpaRepository<MembersDto, Long> {

    MembersDto findByMemberId(String memberId);
     
}
