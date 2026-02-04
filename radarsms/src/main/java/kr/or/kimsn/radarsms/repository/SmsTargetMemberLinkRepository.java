package kr.or.kimsn.radarsms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radarsms.dto.SmsTargetMemberLinkDto;
import kr.or.kimsn.radarsms.dto.pkColumn.SmsTargetMemberLinkPk;

public interface SmsTargetMemberLinkRepository extends JpaRepository<SmsTargetMemberLinkDto, SmsTargetMemberLinkPk>{

}
