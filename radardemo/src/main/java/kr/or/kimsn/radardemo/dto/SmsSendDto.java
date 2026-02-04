package kr.or.kimsn.radardemo.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class SmsSendDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long msg_seq;
    private String req_date;
    private Integer cur_state;
    private String call_to;
    private String call_from;
    private String sms_txt;
    private String msg_type; //4:SMS, 6:MMS
    private String rslt_code2;
    // private String TemplateCode;
    // private Long CONT_SEQ; //mms 발송시 필요
    
}
