package kr.or.kimsn.radarsms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radarsms.dto.ReceiveDataForSiteStatMonthDto;

public interface ReceiveDataForSiteStatMonthRepository extends JpaRepository<ReceiveDataForSiteStatMonthDto, String> {
    //  @Query(
    //      nativeQuery = true,
    //      value=
    //      "select \n"+
    //      "  DATE_FORMAT(data_time, '%Y') as stat_year, \n" +
    //      "  DATE_FORMAT(data_time, '%c') as stat_month, \n" +
    //      "  data_type as data_type, \n"+
    //      "  recv_condition as recv_condition, \n"+
    //      "  count(recv_condition) as cnt  \n"+
    //      "from receive_data \n"+
    //      "where 1=1 \n"+
    //      "  and site = :site \n"+
    //      "  and data_time >= :data_time \n"+
    //      "  and data_time < :recv_time \n"+
    //      "  and recv_condition <> 'INIT' \n"+
    //      "group by DATE_FORMAT(data_time, '%Y'), DATE_FORMAT(data_time, '%c'), data_type, recv_condition"
    //  )
    // @Query(
    //     nativeQuery = true,
    //     value=
    //     "SELECT \n" + 
    //     "  MON.STAT_YEAR AS STAT_YEAR,\n" + 
    //     "  MON.STAT_MONTH AS STAT_MONTH,\n" + 
    //     "  MON.DATA_TYPE AS DATA_TYPE,\n" + 
    //     "  MON.RECV_CONDITION AS RECV_CONDITION,\n" + 
    //     "  MON.CNT AS CNT,\n" + 
    //     "  ROUND((MON.CNT/TOTAL.TOTALCNT)*100) AS PERCENT\n" + 
    //     "FROM (\n" + 
    //     "  SELECT \n" + 
    //     "    DATE_FORMAT(DATA_TIME, '%Y') AS STAT_YEAR,\n" + 
    //     "    DATE_FORMAT(DATA_TIME, '%c') AS STAT_MONTH,\n" + 
    //     "    DATA_TYPE AS DATA_TYPE, \n" + 
    //     "    RECV_CONDITION AS RECV_CONDITION, \n" + 
    //     "    COUNT(RECV_CONDITION) AS CNT  \n" + 
    //     "  FROM receive_data \n" + 
    //     "  WHERE 1=1 \n" + 
    //     "    AND SITE = :site\n" + 
    //     "    AND DATA_TIME >= :data_time \n" + 
    //     "    AND DATA_TIME <  :recv_time\n" + 
    //     "    AND RECV_CONDITION <> 'INIT'\n" + 
    //     "   GROUP BY DATE_FORMAT(DATA_TIME, '%Y'),DATE_FORMAT(DATA_TIME, '%c'), DATA_TYPE, RECV_CONDITION\n" + 
    //     ") MON, (\n" + 
    //     "  SELECT \n" + 
    //     "    STAT_MONTH,\n" + 
    //     "    DATA_TYPE,\n" + 
    //     "    SUM(CNT) AS TOTALCNT\n" + 
    //     "  FROM (\n" + 
    //     "    SELECT \n" + 
    //     "      DATE_FORMAT(DATA_TIME, '%Y') AS STAT_YEAR,\n" + 
    //     "      DATE_FORMAT(DATA_TIME, '%c') AS STAT_MONTH,\n" + 
    //     "      DATA_TYPE AS DATA_TYPE, \n" + 
    //     "      RECV_CONDITION AS RECV_CONDITION, \n" + 
    //     "      COUNT(RECV_CONDITION) AS CNT  \n" + 
    //     "    FROM receive_data \n" + 
    //     "    WHERE 1=1 \n" + 
    //     "      AND SITE = :site\n" + 
    //     "      AND DATA_TIME >= :data_time \n" + 
    //     "      AND DATA_TIME <  :recv_time\n" + 
    //     "      AND RECV_CONDITION <> 'INIT'\n" + 
    //     "    GROUP BY DATE_FORMAT(DATA_TIME, '%Y'), DATE_FORMAT(DATA_TIME, '%c'), DATA_TYPE, RECV_CONDITION\n" + 
    //     " ) TOTA\n" + 
    //     "  GROUP BY STAT_MONTH, TOTA.DATA_TYPE \n" + 
    //     ") TOTAL\n" + 
    //     "WHERE MON.STAT_MONTH = TOTAL.STAT_MONTH \n"
    // )
    @Query(
        nativeQuery = true,
        value = 
        "WITH RECURSIVE cte (n) AS (\n" + 
        "  SELECT 1\n" + 
        "  UNION ALL\n" + 
        "  SELECT n + 1 FROM cte WHERE n < 12\n" + 
        ")\n" + 
        "  select \n" + 
        "    IFNULL(dt.stat_year, 0) as stat_year,\n" + 
        "    IFNULL(dt.stat_month, n) as stat_month,\n" + 
        "    IFNULL(dt.data_type, :data_type) as data_type,\n" + 
        "    IFNULL(dt.recv_condition, :recv_condition) as recv_condition,\n" + 
        "    IFNULL(dt.cnt, 0) as cnt,\n" + 
        "    IFNULL(dt.percent, 0) as percent\n" + 
        "  from cte\n" + 
        "    left outer join (\n" + 
        "      select \n" + 
        "        mon.stat_year as stat_year,\n" + 
        "        mon.stat_month as stat_month,\n" + 
        "        mon.data_type as data_type,\n" + 
        "        mon.recv_condition as recv_condition,\n" + 
        "        mon.cnt as cnt,\n" + 
        "        ROUND((mon.cnt/total.totalcnt)*100) as percent\n" + 
        "      from (\n" + 
        "        select \n" + 
        "          DATE_FORMAT(data_time, '%Y') as stat_year,\n" + 
        "          DATE_FORMAT(data_time, '%c') as stat_month,\n" + 
        "          data_type as data_type, \n" + 
        "          recv_condition as recv_condition, \n" + 
        "          count(recv_condition) as cnt  \n" + 
        "       from receive_data \n" + 
        "       where 1=1 \n" + 
        "         and site = :site\n" + 
        "         and recv_condition = :recv_condition\n" + 
        "         and data_time >= :data_time \n" + 
        "         and data_time <  :recv_time\n" + 
        "         and recv_condition <> 'INIT'\n" + 
        "       group by DATE_FORMAT(data_time, '%Y'),DATE_FORMAT(data_time, '%c'), data_type, recv_condition\n" + 
        "  ) mon, (\n" + 
        "    SELECT \n" + 
        "      stat_month,\n" + 
        "      data_type,\n" + 
        "      sum(cnt) as totalcnt\n" + 
        "    FROM (\n" + 
        "      select \n" + 
        "        DATE_FORMAT(data_time, '%Y') as stat_year,\n" + 
        "        DATE_FORMAT(data_time, '%c') as stat_month,\n" + 
        "        data_type as data_type, \n" +
        "        recv_condition as recv_condition, \n" + 
        "        count(recv_condition) as cnt  \n" + 
        "      from receive_data \n" + 
        "      where 1=1 \n" + 
        "        and site = :site\n" + 
        "        and data_time >= :data_time \n" + 
        "        and data_time <  :recv_time\n" + 
        "        and recv_condition <> 'INIT'\n" + 
        "      group by DATE_FORMAT(data_time, '%Y'), DATE_FORMAT(data_time, '%c'), data_type, recv_condition\n" + 
        "   ) tota\n" + 
        "    group by stat_month, tota.data_type \n" + 
        "  ) total\n" + 
        "  where mon.stat_month = total.stat_month\n" + 
        "  ) dt\n" + 
        "on n = dt.stat_month"
    )
    List<ReceiveDataForSiteStatMonthDto> getReceiveDataForSiteStatMonth(
        @Param("site") String site,
        @Param("data_time") String data_time,
        @Param("recv_time") String recv_time,
        @Param("recv_condition") String recv_condition,
        @Param("data_type") String data_type
    );
}
