package kr.or.kimsn.radar.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kr.or.kimsn.radar.data.dto.SmsTargetGroupLinkDto;
import kr.or.kimsn.radar.data.dto.pkColumn.SmsTargetGroupLinkPk;

public interface SmsTargetGroupLinkRepository extends JpaRepository<SmsTargetGroupLinkDto, SmsTargetGroupLinkPk>{
}
