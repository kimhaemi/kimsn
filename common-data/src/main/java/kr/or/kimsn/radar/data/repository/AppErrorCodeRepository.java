package kr.or.kimsn.radar.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radar.data.dto.AppErrorCodeDto;
import kr.or.kimsn.radar.data.dto.pkColumn.AppErrorCodePk;

public interface AppErrorCodeRepository extends JpaRepository<AppErrorCodeDto, AppErrorCodePk> {

}
