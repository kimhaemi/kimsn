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
public class ReceiveSettingPk implements Serializable{
    private String dataKind;
    private String dataType;
}
