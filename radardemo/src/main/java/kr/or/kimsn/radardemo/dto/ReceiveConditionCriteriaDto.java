package kr.or.kimsn.radardemo.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import kr.or.kimsn.radardemo.dto.pkColumn.ReceiveConditionCriteriaPk;
import lombok.Data;

@Data
@Entity
@Table(name = "receive_condition_criteria", catalog = "watchdog")
@IdClass(ReceiveConditionCriteriaPk.class)
public class ReceiveConditionCriteriaDto {
    
    @Id
    private String code;
    private String name;
    private int criterion;
    private String comment;
    @Id
    private int gubun;
    @Id
    private String codedtl;
    private String sort;
}
