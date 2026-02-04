package kr.or.kimsn.radarsms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radarsms.dto.SmsSendOnOffDto;

public interface SmsSendOnOffRepository extends JpaRepository<SmsSendOnOffDto, String>{
    
}
