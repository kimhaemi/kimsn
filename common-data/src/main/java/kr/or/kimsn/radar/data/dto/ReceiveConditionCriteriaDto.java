package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import kr.or.kimsn.radar.data.dto.pkColumn.ReceiveConditionCriteriaPk;
import lombok.Data;

@Data
@Entity
@Table(name = "receive_condition_criteria")
@IdClass(ReceiveConditionCriteriaPk.class)
public class ReceiveConditionCriteriaDto {
    
    @Id
    private String code;
    private String name;
    private int criterion;
    private String comment;
    @Id
    private Integer gubun;
    @Id
    private String codedtl;
    private String sort;
    private int status;
}
