package kr.or.kimsn.radarsms.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import kr.or.kimsn.radarsms.dto.pkColumn.AppErrorCodePk;
import lombok.Data;

@Data
@Entity
@IdClass(AppErrorCodePk.class)
@Table(name = "app_error_code", catalog = "nuri")
public class AppErrorCodeDto {
    @Id
    private String app_gubun;
    @Id
    private String app_code;
    private String message;
    private String description;
}
