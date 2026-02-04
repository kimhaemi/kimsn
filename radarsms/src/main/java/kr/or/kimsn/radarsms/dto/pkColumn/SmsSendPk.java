package kr.or.kimsn.radarsms.dto.pkColumn;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendPk implements Serializable {
    private Long msg_seq;
    private String msg;
}
