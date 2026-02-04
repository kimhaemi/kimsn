package kr.or.kimsn.radardemo.dto;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class SmsSendMemberDto {
    private String name;
    @Id
    private String phone_num;
    private int warn; //경고 -- MISS
    private int retr; //복구 -- 장애 복구, 네트워크 복구
    private int sms; //문자 발송 여부
    private int tota; //네트워크 오류
}
