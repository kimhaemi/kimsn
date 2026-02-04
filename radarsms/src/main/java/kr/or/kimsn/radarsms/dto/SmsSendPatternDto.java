package kr.or.kimsn.radarsms.dto;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import kr.or.kimsn.radarsms.dto.pkColumn.SmsSendPatternPk;
import lombok.Data;

@Data
@Entity
@Table(name = "sms_send_pattern")
@IdClass(SmsSendPatternPk.class)
public class SmsSendPatternDto {
    
    // @EmbeddedId
    // private SmsSendPatternPk smsSendPatternPk;

    @Id
    private String code;
    @Id
    private String mode;
    private String activation;
    private String pattern;
    @Id
    private String codedtl;
}

