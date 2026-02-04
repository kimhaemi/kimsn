package kr.or.kimsn.radardemo.common;

import java.util.Properties;
import java.util.Vector;

import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SftpUtil {

    private JSch jSch;
    private Session session;
    private Channel channel;
    private ChannelSftp channelSftp;

    // sftp 서버 연결
    public boolean open(String host, String id, String password, int port) {
        boolean result = false;

        int connectTimeOut = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "connectTimeOut"));
        int sessionTimeOut = Integer.parseInt(DataCommon.getInfoConf("ipInfo", "sessionTimeOut"));

        // JSch 객체를 생성
        jSch = new JSch();

        try {
            // privateKey 인증
            // jSch.addIdentity(privateKey);

            // JSchSession 객체를 생성 (사용자 이름, 접속할 호스트, 포트 전달)
            session = jSch.getSession(id, host, port);
            session.setPassword(password);

            // 기타 설정 적용
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // 접속
            session.connect(sessionTimeOut * 1000);

            // sftp 채널 열기 및 접속
            channel = session.openChannel("sftp");
            channel.connect(connectTimeOut * 1000); // 10초
            result = true;

            // 채널을 FTP 용 채널 객체로 캐스팅
            channelSftp = (ChannelSftp) channel;

        } catch (JSchException e) {
            result = false;
            // e.printStackTrace();
            // log.info("SFTP: server connect failed.");
        }

        return result;
    }

    // sftp 서버 연결 종료
    public void close() {
        if (session != null) {
            session.disconnect();
        }
        if (channel != null) {
            channel.disconnect();
        }
        if (channelSftp != null) {
            channelSftp.disconnect();
        }
    }

    /**
     * 디렉토리( or 파일) 존재 여부
     * 
     * @param path 디렉토리 (or 파일)
     * @return
     */
    public boolean fileExists(String path, String fileName, String siteCd, String dataKind, String filePattern,
            String timeZone) {
        log.info("[파일 경로] : " + path);

        Vector res = null;
        try {
            res = channelSftp.ls(path + "/" + fileName);
            log.info("[파일 존재여부 확인]  : " + res.size());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
        }
        return res != null && !res.isEmpty();
    }

    // 파일 사이즈
    public Long fileSize(String path, String fileName, Long file_size_min, Long file_size_max) {
        log.info("[file size check]");
        Long fileSize = 0L;
        try {
            fileSize = channelSftp.lstat(path + "/" + fileName).getSize();

            // log.info("[파일 size] : " + fileSize);
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return 0L;
            }
            // } catch (IOException e) {
            // log.info("file size error : " + e);
            // return 0L;
        }
        return fileSize;
    }

}
