package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radarsms.dto.ReceiveDataDto;
import kr.or.kimsn.radarsms.dto.pkColumn.ReceiveDataPk;

public interface ReceiveDataRepository extends JpaRepository<ReceiveDataDto, ReceiveDataPk> {

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
            "    file_size  \n" +
            "from receive_data \n" +
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
            @Param("dateClose") String dateClose);

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
            "    file_size  \n" +
            "from receive_data \n" +
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
}
