package kr.or.kimsn.radar.data.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radar.data.dto.StationStatusDto;

public interface StationStatusRepository extends JpaRepository<StationStatusDto, String>{

    List<StationStatusDto> findByOrderBySortOrder();

    StationStatusDto findBySiteCd(String sitecd);

    List<StationStatusDto> findByGubun(int gubun);

    @Query(
        nativeQuery = true,
        value=
        "update station_status set \n" +
		"  site_status = :site_status \n" +
        "where 1=1  \n" +
        "  and site_cd = :site_cd \n"
    )
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    // 지점별 운영상태 설정 일괄 수정
    Integer setStationStatusModify(
        @Param("site_cd") String site_cd,
        @Param("site_status") String site_status
    );
    
}
