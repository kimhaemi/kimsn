package kr.or.kimsn.radar.data.dto;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import kr.or.kimsn.radar.data.dto.pkColumn.SmsSendPatternPk;
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
    private int activation;
    private String pattern;
    @Id
    private String codedtl;

    private int status;
}

