package kr.or.kimsn.radarsms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radarsms.dto.MembersDto;

public interface MembersRepository extends JpaRepository<MembersDto, Long> {

    MembersDto findByMemberId(String memberId);
     
}
