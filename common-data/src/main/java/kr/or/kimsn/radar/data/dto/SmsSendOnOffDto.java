package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "sms_send_onoff")
public class SmsSendOnOffDto {
    
    @Id
    private String code; 
    private Integer value;
}
