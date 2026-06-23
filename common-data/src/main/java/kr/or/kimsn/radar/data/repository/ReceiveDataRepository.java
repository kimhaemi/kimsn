package kr.or.kimsn.radar.data.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radar.data.dto.ReceiveDataDto;
import kr.or.kimsn.radar.data.dto.pkColumn.ReceiveDataPk;
import org.springframework.transaction.annotation.Transactional;

public interface ReceiveDataRepository extends JpaRepository<ReceiveDataDto, ReceiveDataPk> {

    // app contents seq
    @Query(nativeQuery = true, value = "select DATE_FORMAT(now()-interval :second second, '%Y%m%d%H%i') as previousTime from dual"
        // select DATE_FORMAT(now()-interval 60*4+30 second, '%Y%m%d%H%i') as
        // previousTime from dual;
    )
    // 특정 시간 구하기(초단위)
    String getPreviousTime(@Param("second") int second);

    @Query(nativeQuery = true, value = "select \n" +
            "    data_kind,  \n" +
            "    site,  \n" +
            "    data_type,  \n" +
            "    data_time, \n" +
            "    data_kst,  \n" +
            "    recv_time,  \n" +
            "    recv_condition,  \n" +
            "    recv_condition_check_time,  \n" +
            "    file_name,  \n" +
            "    file_size,  \n" +
            "    codedtl  \n" +
            "from watchdog.receive_data \n" +
            "where 1=1 \n" +
            "  and site       = :site \n" +
            "  and data_kind  = :data_kind \n" +
            "  and data_type  = :data_type \n" +
            "  and data_kst >= :dateStart \n" +
            "  and data_kst <= :dateClose \n" +
            "order by data_kind, site, data_type, data_time desc \n")
    // 지점별 과거자료 검색
    List<ReceiveDataDto> getReceiveDataList(
            @Param("data_kind") String data_kind,
            @Param("data_type") String data_type,
            @Param("site") String site,
            @Param("dateStart") String dateStart,
            @Param("dateClose") String dateClose
    );

    @Query(nativeQuery = true, 
        value = "select \n" +
            "    data_kind,  \n" +
            "    site,  \n" +
            "    data_type,  \n" +
            "    data_time, \n" +
            "    data_kst,  \n" +
            "    recv_time,  \n" +
            "    recv_condition,  \n" +
            "    recv_condition_check_time,  \n" +
            "    file_name,  \n" +
            "    file_size,  \n" +
            "    codedtl \n" +
            "from watchdog.receive_data \n" +
            "where 1=1 \n" +
            "  and site = :site \n" +
            "  and data_kind  = :data_kind \n" +
            "  and data_type  = 'NQC' \n" +
            "order by data_kst desc, data_kind, site, data_type \n") // limit 제거
    List<ReceiveDataDto> getReceiveDataList(
        @Param("site") String site,
        @Param("data_kind") String data_kind,
        Pageable pageable); // Pageable 추가

    @Query(nativeQuery = true, 
        value = "select \n" +
            "    data_kind,  \n" +
            "    site,  \n" +
            "    data_type,  \n" +
            "    data_time, \n" +
            "    data_kst,  \n" +
            "    recv_time,  \n" +
            "    recv_condition,  \n" +
            "    recv_condition_check_time,  \n" +
            "    file_name,  \n" +
            "    file_size,  \n" +
            "    codedtl \n" +
            "from watchdog.receive_data \n" +
            "where 1=1 \n" +
            "  and site = :site \n" +
            "  and data_kind  = :data_kind \n" +
            "  and data_type  = 'NQC' \n" +
            "order by data_kst desc, data_kind, site, data_type \n"+
            "limit :count"
        ) // limit 제거
    List<ReceiveDataDto> getReceiveDataListLimit(
        @Param("site") String site,
        @Param("data_kind") String data_kind,
        @Param("count") int count);


    @Query(nativeQuery = true, value = "select \n" +
        "    data_kind,  \n" +
        "    site,  \n" +
        "    data_type,  \n" +
        "    data_time, \n" +
        "    data_kst,  \n" +
        "    recv_time,  \n" +
        "    recv_condition,  \n" +
        "    recv_condition_check_time,  \n" +
        "    file_name,  \n" +
        "    file_size,  \n" +
        "    codedtl \n" +
        "from watchdog.receive_data \n" +
        "where 1=1 \n" +
        "  and site = :site \n" +
        "  and data_kind  = :data_kind \n" +
        "  and data_type  = 'NQC' \n" +
        "  and codedtl  = :codedtl \n" +
        "order by data_kind, site, data_type, data_kst desc \n")
        // 지점별 과거자료 검색
    List<ReceiveDataDto> getReceiveDataCodedtlList(
        @Param("site") String site,
        @Param("data_kind") String data_kind,
        @Param("codedtl") String codedtl,
        Pageable pageable // Pageable 추가
        // @Param("data_type") String data_type,
        // @Param("dateStart") String dateStart,
        // @Param("dateClose") String dateClose
    );

    @Query(nativeQuery = true, value = "select \n" +
            "    data_kind,  \n" +
            "    site,  \n" +
            "    data_type,  \n" +
            "    data_time, \n" +
            "    data_kst,  \n" +
            "    recv_time,  \n" +
            "    recv_condition,  \n" +
            "    recv_condition_check_time,  \n" +
            "    file_name,  \n" +
            "    file_size,  \n" +
            "    codedtl  \n" +
            "from watchdog.receive_data \n" +
            "where 1=1 \n" +
            "  and data_kst <= :now \n" +
            "  and data_kst >= subdate( :now , interval 3 hour ) \n" +
            "  and data_kind  = :data_kind \n" +
            "  and site       = :site \n" +
            "  and data_type  = :data_type \n" +
            "order by data_kind, site, data_type, data_kst desc \n"

    )
    // 3시간 전까지 data
    List<ReceiveDataDto> getReceiveDataThreeHour(
            @Param("now") String now,
            @Param("data_kind") String data_kind,
            @Param("site") String site,
            @Param("data_type") String data_type);

    @Query(nativeQuery = true, value = "update watchdog.receive_data set  \n" +
        "    recv_condition = :new_recv_condition \n" +
        "  , codedtl = :new_codedtl \n" +
        // " recv_condition_check_time = now() \n" +
        "where 1=1  \n" +
        "  and site = :site -- param \n" +
        "  and data_kind  = :dataKindStr -- param \n" +
        "  and data_type  = :dataType -- param \n" +
        "  and recv_condition = :where_recv_condition -- param \n" +
        "  and DATE_FORMAT(data_kst, '%Y%m%d%H%i') = DATE_FORMAT(:data_kst,'%Y%m%d%H%i')\n"
        // " order by data_kst desc limit 1 \n"
        // " and DATE_FORMAT(data_kst, '%Y%m%d%H%i') =
        // DATE_FORMAT(:data_kst,'%Y%m%d%H%i')\n"
    )
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)//쿼리 실행 후 캐시를 강제로 비움. //강제 push
        // 결과 이력 update
    Integer updateReceiveData(
        @Param("new_recv_condition") String new_recv_condition,
        @Param("new_codedtl") String new_codedtl,
        @Param("site") String site,
        @Param("dataKindStr") String dataKindStr,
        @Param("dataType") String dataType,
        @Param("where_recv_condition") String where_recv_condition,
        @Param("data_kst") String data_kst
    );
}
