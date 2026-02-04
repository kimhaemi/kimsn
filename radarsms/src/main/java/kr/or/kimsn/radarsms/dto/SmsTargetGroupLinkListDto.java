package kr.or.kimsn.radarsms.dto;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class SmsTargetGroupLinkListDto {
    @Id
    private String site;
    private String dataKind;
    private String dataType;
    private String group_id;

    private String name_kr;
    private String data_name;
}
