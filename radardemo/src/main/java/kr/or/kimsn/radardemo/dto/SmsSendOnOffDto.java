package kr.or.kimsn.radardemo.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "sms_send_onoff", catalog = "watchdog")
public class SmsSendOnOffDto {
    
    @Id
    private String code; 
    private Integer value;
}
