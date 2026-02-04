package kr.or.kimsn.radardemo.dto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radardemo.dto.ReceiveSettingDto;

public interface ReceiveSettingRepository extends JpaRepository<ReceiveSettingDto, String> {

    List<ReceiveSettingDto> findByDataKind(String dataKind);

    ReceiveSettingDto findByDataKindAndPermittedWatch(String dataKind, Integer permittedWatch);

    ReceiveSettingDto findByDataKindAndPermittedWatchAndStatus(String dataKind, Integer permittedWatch, Integer status);

    List<ReceiveSettingDto> findByOrderByDataKindAscPermittedWatchDesc();

}
