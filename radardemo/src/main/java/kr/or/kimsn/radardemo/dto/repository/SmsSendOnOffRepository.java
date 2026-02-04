package kr.or.kimsn.radardemo.dto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radardemo.dto.SmsSendOnOffDto;

public interface SmsSendOnOffRepository extends JpaRepository<SmsSendOnOffDto, String>{

    SmsSendOnOffDto findByCode(String code);
}
