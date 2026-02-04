package kr.or.kimsn.radarsms.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import kr.or.kimsn.radarsms.dto.pkColumn.ReceiveDataPk;
import lombok.Data;

@Data
@Entity
@IdClass(ReceiveDataPk.class)
public class ReceiveDataDto {
    
    @Id
    private String data_kind;
    @Id
    private String site;
    @Id
    private String data_type;
    @Id
    private String data_time;
    @Id
    private String data_kst;
    private String recv_time;
    private String recv_condition;
    private String recv_condition_check_time;
    private String file_name;
    private Integer file_size;

}
