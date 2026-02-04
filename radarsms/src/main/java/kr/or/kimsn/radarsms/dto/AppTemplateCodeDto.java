package kr.or.kimsn.radarsms.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
