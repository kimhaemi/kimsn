package kr.or.kimsn.radardemo.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
// import javax.persistence.GeneratedValue;
// import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "station_rdr", catalog = "watchdog")
public class StationDto {

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;

    @Id
    @Column(name = "site_cd")
    private String siteCd;
    private String site_num;
    private String name_kr;
    private String name_en;
    private String height;
    private String max_range;
    private String gate_size;
    private String gates;
    private String rain_intensity;
    private String addr;
    private String model;
    private String install_date;
    private String prod_company;
    private String prod_country;
    private Integer permitted_watch;
    @Column(name = "sort_order")
    private String sortOrder;
    private Integer gubun;
    // private String ip;
    // private String user_id;
    // private String pwd;

}
