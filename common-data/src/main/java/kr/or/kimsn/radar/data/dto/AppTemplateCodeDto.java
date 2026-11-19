package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "app_template_code", catalog = "nuri")
public class AppTemplateCodeDto {
    
    private String appGubun;
    @Id
    private String templateCode;
    private String head;
    private String foot;
    private String title;
    private String useButton;
}
