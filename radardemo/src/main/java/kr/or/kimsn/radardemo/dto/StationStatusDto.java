package kr.or.kimsn.radardemo.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "station_status", catalog = "watchdog")
public class StationStatusDto {

    @Id
    @Column(name = "site_cd")
    private String siteCd;
    private String site_name;
    private String site_status;
    private int sort_order;
    private int gubun;
    private int status;

}
