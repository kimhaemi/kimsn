package kr.or.kimsn.radardemo.common;

import java.io.FileInputStream;
import java.util.Properties;

public class DataCommon {

    // local
     private final static String siteInfo = "./conf/siteInfoSetting.conf";
     private final static String ipInfo = "./conf/siteIPInfoSetting.conf";

    // 대형
//    private final static String siteInfo = "/home/watcher/deamon/RDR/conf/siteInfoSetting.conf";
//    private final static String ipInfo = "/home/watcher/ipConf/RDR/siteIPInfoSetting.conf";

    // 소형
    // private final static String siteInfo =
    // "/home/watcher/deamon/SDR/conf/siteInfoSetting.conf";
    // private final static String ipInfo =
    // "/home/watcher/ipConf/SDR/siteIPInfoSetting.conf";

    // 공항
    // private final static String siteInfo =
    // "/home/watcher/deamon/TDWR/conf/siteInfoSetting.conf";
    // private final static String ipInfo =
    // "/home/watcher/ipConf/TDWR/siteIPInfoSetting.conf";

    public static String getInfoConf(String infoStr, String tp) {
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