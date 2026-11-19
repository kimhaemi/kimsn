package kr.or.kimsn.radar.data.dto.pkColumn;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveDataForSiteStatMonthPk implements Serializable{
    Integer stat_year;
    Integer stat_month;
    String data_type;
    String recv_condition;
}
