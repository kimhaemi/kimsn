package kr.or.kimsn.radardemo.dto.pkColumn;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveConditionCriteriaPk implements Serializable{
    private String code;
    private Integer gubun;
    private String codedtl;
}

