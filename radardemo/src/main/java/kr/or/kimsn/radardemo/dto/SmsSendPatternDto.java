package kr.or.kimsn.radardemo.dto;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import kr.or.kimsn.radardemo.dto.pkColumn.SmsSendPatternPk;
import lombok.Data;

@Data
@Entity
@Table(name = "sms_send_pattern", catalog = "watchdog")
@IdClass(SmsSendPatternPk.class)
public class SmsSendPatternDto {
    
    // @EmbeddedId
    // private SmsSendPatternPk smsSendPatternPk;

    @Id
    private String code;
    @Id
    private String mode;
    private int activation;
    private String pattern;
    @Id
    private String codedtl;
    private int status;

}

