package kr.or.kimsn.radar.daemon.config;

import java.io.FileInputStream;
import java.util.Properties;

public class DataCommon {

    public static String getInfoConf(String infoStr, String tp, String siteInfo, String ipInfo) {
        String confInfo = "";

        FileInputStream ipInfoIn = null;

        try {
            Properties ipInfoProps = new Properties();
            if (infoStr.equals("siteInfo"))
                ipInfoIn = new FileInputStream(siteInfo);
            if (infoStr.equals("ipInfo"))
                ipInfoIn = new FileInputStream(ipInfo);

            ipInfoProps.load(ipInfoIn);
            confInfo = ipInfoProps.getProperty(tp);

        } catch (Exception e) {
            System.out.println("사이트 Info 환경설정 정보 읽어오기 실패!!");
        }
        return confInfo;
    }

}