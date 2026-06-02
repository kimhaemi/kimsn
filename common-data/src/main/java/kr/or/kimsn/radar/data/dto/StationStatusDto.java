package kr.or.kimsn.radar.data.dto;

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
    @Column(name = "site_cd")     private String siteCd;
    @Column(name = "site_name")   private String siteName;
    @Column(name = "site_status") private String siteStatus;
    @Column(name = "sort_order")  private String sortOrder;
    private int gubun;
    private int status;

}
