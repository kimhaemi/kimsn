package kr.or.kimsn.radarsms.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import kr.or.kimsn.radarsms.dto.pkColumn.ReceiveDataForSiteStatMonthPk;
import lombok.Data;

@Data
@Entity
@IdClass(ReceiveDataForSiteStatMonthPk.class)
public class ReceiveDataForSiteStatMonthDto {
    @Id
    Integer stat_year;
    @Id
    Integer stat_month;
    String data_type;
    @Id
    String recv_condition;
    Integer cnt;
    Integer percent;
}
