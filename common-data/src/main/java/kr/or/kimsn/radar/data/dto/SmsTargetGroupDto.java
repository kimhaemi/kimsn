package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "sms_target_group", catalog = "watchdog")
public class SmsTargetGroupDto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gid")
    private Long id;
    
    private String name;
    private String activation;
    private String status;
    @Column(name="sort_order")
    private Integer sortOrder;

    private int gubun;
    @Column(name="agency_cd")
    private String agencyCd;

}
