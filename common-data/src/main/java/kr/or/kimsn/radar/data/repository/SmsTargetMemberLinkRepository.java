package kr.or.kimsn.radar.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radar.data.dto.SmsTargetMemberLinkDto;
import kr.or.kimsn.radar.data.dto.pkColumn.SmsTargetMemberLinkPk;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsTargetMemberLinkRepository extends JpaRepository<SmsTargetMemberLinkDto, SmsTargetMemberLinkPk>{

}
