package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import kr.or.kimsn.radar.data.dto.pkColumn.AppErrorCodePk;
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
