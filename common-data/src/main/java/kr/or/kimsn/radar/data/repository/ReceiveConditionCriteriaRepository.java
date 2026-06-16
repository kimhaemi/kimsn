package kr.or.kimsn.radar.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radar.data.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radar.data.dto.pkColumn.ReceiveConditionCriteriaPk;

public interface ReceiveConditionCriteriaRepository extends JpaRepository<ReceiveConditionCriteriaDto, ReceiveConditionCriteriaPk> {

    //경고기준설정 조회
    List<ReceiveConditionCriteriaDto> findByOrderByGubunAscSortAsc();

    //경고기준설정 조회 - site 구분
    List<ReceiveConditionCriteriaDto> findByGubunOrderBySort(int gubun);

    @Query(
        nativeQuery = true,
        value =
            "select\n" +
                "  code\n" +
                "  , name\n" +
                "  , criterion\n" +
                "  , comment\n" +
                "  , gubun\n" +
                "  , codedtl\n" +
                "  , sort\n" +
                "from watchdog.receive_condition_criteria rcc\n" +
                "where 1=1\n" +
                "  and code != 'ORDI'\n" +
                "  and gubun = :gubun -- 1 \n" +
                "  and code = :code -- 'WARN'\n" +
                "  and codedtl = :codedtl -- 'filesize_no'\n"
    )
        //경고기준설정 조회(단건)
    ReceiveConditionCriteriaDto getReceiveConditionCriteria(
        @Param("gubun") int gubun,
        @Param("code") String code,
        @Param("codedtl") String codedtl
    );

    @Query(
        nativeQuery = true,
        value=
        "update receive_condition_criteria set \n"+
        "    criterion = :criterion \n"+
        "where 1=1\n"+
        "  and code = :code \n" +
        "  and codedtl = :codedtl \n" +
        "  and gubun = :gubun \n"
    )
    @Transactional
    @Modifying
    // 경고 기준 설정 일괄 수정
    Integer setReceiveConditionCriteriaModify(
        @Param("criterion") int criterion,
        @Param("code") String code,
        @Param("gubun") Integer gubun,
        @Param("codedtl") String codedtl
    );
    
}
