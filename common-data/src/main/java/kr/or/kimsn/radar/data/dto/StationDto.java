package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "station_rdr")
public class StationDto {

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;

    @Id
    @Column(name = "site_cd")
    private String siteCd;
    @Column(name = "site_num")
    private Long siteNum;
    @Column(name = "name_kr")
    private String nameKr;
    @Column(name = "name_en")
    private String nameEn;
    private String height;
    @Column(name = "max_range")
    private String maxRange;
    @Column(name = "gate_size")
    private String gateSize;
    private String gates;
    @Column(name = "rain_intensity")
    private String rainIntensity;
    private String addr;
    private String model;
    @Column(name = "install_date")
    private String installDate;
    @Column(name = "prod_company")
    private String prodCompany;
    @Column(name = "prod_country")
    private String prodCountry;
    @Column(name = "permitted_watch")
    private Integer permittedWatch;
    @Column(name = "sort_order")
    private Integer sortOrder;
    private Integer gubun;

    @Column(name = "agency_cd")
    private String agencyCd;

    @Column(name = "style_attr")
    private String styleAttr;

    private int status;

}
