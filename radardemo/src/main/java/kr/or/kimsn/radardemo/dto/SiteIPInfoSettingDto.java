package kr.or.kimsn.radardemo.dto;

// import javax.persistence.Entity;

import lombok.Data;

@Data
// @Entity
public class SiteIPInfoSettingDto {
    
    // 공통 영역
    private int PORT; //= 22

    // # 대형 레이더
    // # 관악산
    private String KWK_IP; //= 190.1.56.113
    // # KWK_PORT= 22
    private String KWK_ID; //
    private String KWK_PASSWORD; //

    // # 백령도
    private String BRI_IP; //= 190.1.11.103
    // # BRI_PORT= 22
    private String BRI_ID; //
    private String BRI_PASSWORD; //

    // # 광덕산
    private String GDK_IP; //= 190.1.218.113
    // # GDK_PORT= 22
    private String GDK_ID; //
    private String GDK_PASSWORD; //

    // # 강릉
    private String GNG_IP; //= 190.1.219.113
    // # GNG_PORT= 22
    private String GNG_ID; //
    private String GNG_PASSWORD; //

    // # 면봉산
    private String MYN_IP; //= 190.1.126.113
    // # MYN_PORT= 22
    private String MYN_ID; //
    private String MYN_PASSWORD; //

    // # 구덕산
    private String PSN_IP; //= 190.1.110.113
    // # PSN_PORT= 22
    private String PSN_ID; //
    private String PSN_PASSWORD; //

    // # 오성산
    private String KSN_IP; //= 190.1.150.113
    // # KSN_PORT= 22
    private String KSN_ID; //
    private String KSN_PASSWORD; //

    // # 진도
    private String JNI_IP; //= 190.1.137.113
    // # JNI_PORT= 22
    private String JNI_ID; //
    private String JNI_PASSWORD; //

    // # 고산
    private String GSN_IP; //= 190.1.52.113
    // # GSN_PORT= 22
    private String GSN_ID; //
    private String GSN_PASSWORD; //

    // # 성산
    private String SSP_IP; //= 190.1.54.113
    // # SSP_PORT= 22
    private String SSP_ID; //
    private String SSP_PASSWORD; //

    // # 용인
    private String YIT_IP; //= 190.1.59.113
    // # YIT_PORT= 22
    private String YIT_ID; //
    private String YIT_PASSWORD; //

    // # 소형 레이더
    // # 망일산
    private String MIL_IP; //
    // # MIL_PORT= 22
    private String MIL_ID; //
    private String MIL_PASSWORD; //

    // # 수리산
    private String SRI_IP; //
    // # SRI_PORT= 22
    private String SRI_ID; //
    private String SRI_PASSWORD; //

    // # 덕적도
    private String DJK_IP; //
    // # DJK_PORT= 22
    private String DJK_ID; //
    private String DJK_PASSWORD; //

    // # 공항 레이더
    // # 인천공항
    private String IIA_IP; //= 190.1.11.209
    // # IIA_PORT= 22
    private String IIA_ID; //= rainbow
    private String IIA_PASSWORD; //= rain99

}
