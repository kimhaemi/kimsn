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
public class ReceiveDataPk implements Serializable{
    private String data_kind;
    private String site;
    private String data_type;
    private String data_time;
    private String data_kst;
}
