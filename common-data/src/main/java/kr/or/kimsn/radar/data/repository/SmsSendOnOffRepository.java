package kr.or.kimsn.radar.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radar.data.dto.SmsSendOnOffDto;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsSendOnOffRepository extends JpaRepository<SmsSendOnOffDto, String>{
  SmsSendOnOffDto findByCode(String code);
}
