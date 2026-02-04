package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radarsms.dto.ReceiveSettingDto;

public interface ReceiveSettingRepository extends JpaRepository<ReceiveSettingDto, String> {

    List<ReceiveSettingDto> findByDataKind(String dataKind);

    ReceiveSettingDto findByDataKindAndPermittedWatch(String dataKind, Integer permittedWatch);

    List<ReceiveSettingDto> findByOrderByDataKindAscPermittedWatchDesc();

    @Query(
        nativeQuery = true,
        value=
        "update receive_setting set  \n"+
		"  time_zone = :time_zone, \n"+
		"  filename_pattern = :filename_pattern, \n"+
		"  filename_regex   = :filename_regex, \n"+
		"  delay_tolerance  = :delay_tolerance, \n"+
		"  permitted_watch  = :permitted_watch \n"+
	    "where 1=1 \n"+
		"  and data_kind = :data_kind \n"+
		"  and data_type = :data_type \n"
    )
    @Transactional
    @Modifying
    //자료 수신 감시 설정 일괄 수정
    Integer setReceiveSettingModify(
        @Param("time_zone") String time_zone,
        @Param("filename_pattern") String filename_pattern,
        @Param("filename_regex") String filename_regex,
        @Param("delay_tolerance") Integer delay_tolerance,
        @Param("permitted_watch") Integer permitted_watch,
        @Param("data_kind") String data_kind,
        @Param("data_type") String data_type
    );

}
