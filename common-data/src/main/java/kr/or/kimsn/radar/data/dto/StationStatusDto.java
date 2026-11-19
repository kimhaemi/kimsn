package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "station_status", catalog = "watchdog")
public class StationStatusDto {

    @Id
    @Column(name = "site_cd")     private String siteCd;
    @Column(name = "site_name")   private String siteName;
    @Column(name = "site_status") private String siteStatus;
    @Column(name = "sort_order")  private int sortOrder;
    private int gubun;
    private int status;
    @Column(name = "agency_cd")  private String agencyCd;

}
