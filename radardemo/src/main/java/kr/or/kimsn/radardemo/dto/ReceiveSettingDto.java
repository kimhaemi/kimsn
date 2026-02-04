package kr.or.kimsn.radardemo.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import kr.or.kimsn.radardemo.dto.pkColumn.ReceiveSettingPk;
import lombok.Data;

@Data
@Entity
@Table(name = "receive_setting", catalog = "watchdog")
@IdClass(ReceiveSettingPk.class)
public class ReceiveSettingDto {

    @Id
    @Column(name = "data_type")
    private String dataType;
    @Id
    @Column(name = "data_kind")
    private String dataKind;

    private String data_name;
    private String time_zone;
    private String filename_pattern;
    private String filename_regex;
    private Integer recv_interval;
    private Integer delay_tolerance;
    @Column(name = "permitted_watch")
    private Integer permittedWatch;
    private Integer status;

}
