package kr.or.kimsn.radardemo.dto;

// import javax.persistence.Entity;

import lombok.Data;

@Data
// @Entity
public class SiteInfoSettingDto {

    //file path
    private String rdr_path;

    //file size
    private Long file_size_min;
    private Long file_size_max;

    //DB info
    private String dbName;
    private String dbDriverName;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    
}
