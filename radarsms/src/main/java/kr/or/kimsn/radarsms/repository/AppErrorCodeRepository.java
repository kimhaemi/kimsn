package kr.or.kimsn.radarsms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radarsms.dto.AppErrorCodeDto;
import kr.or.kimsn.radarsms.dto.pkColumn.AppErrorCodePk;

public interface AppErrorCodeRepository extends JpaRepository<AppErrorCodeDto, AppErrorCodePk> {

}
