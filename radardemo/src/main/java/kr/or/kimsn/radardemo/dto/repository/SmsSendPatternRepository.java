package kr.or.kimsn.radardemo.dto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radardemo.dto.SmsSendPatternDto;
import kr.or.kimsn.radardemo.dto.pkColumn.SmsSendPatternPk;

public interface SmsSendPatternRepository extends JpaRepository<SmsSendPatternDto, SmsSendPatternPk> {
    List<SmsSendPatternDto> findAll();

    List<SmsSendPatternDto> findByOrderByCodeAscModeDesc();

    // SmsSendPatternDto findByActivationAndStatusAndModeAndCodeAndCodedtl(int
    // activation, int status, String mode, String code, String codedtl);
    List<SmsSendPatternDto> findByActivationAndStatusAndCodeAndCodedtl(int activation, int status, String code,
            String codedtl);

}
