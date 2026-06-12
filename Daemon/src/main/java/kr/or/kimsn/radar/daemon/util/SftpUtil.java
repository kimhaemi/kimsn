package kr.or.kimsn.radar.daemon.util;

import java.util.Properties;
import java.util.Vector;
import lombok.extern.slf4j.Slf4j;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Slf4j
public class SftpUtil {

  private JSch jSch;
  private Session session;
  private Channel channel;
  private ChannelSftp channelSftp;

  /**
   * 💡 [인자 규격 일치화] StepOneProcess에서 호출하는 6개 파라미터 사양에 완벽 정합하도록 시그니처 확장 및 수정
   */
  public boolean open(String host, String id, String password, int port, int connectTimeOut, int sessionTimeOut) {
    jSch = new JSch();

    try {
      // 사용자 정보 기반 원격 SFTP 세션 생성
      session = jSch.getSession(id, host, port);
      session.setPassword(password);

      // 호스트 키 검증 스킵 프로퍼티 기본 안전 바인딩
      Properties config = new Properties();
      config.put("StrictHostKeyChecking", "no");
      session.setConfig(config);

      // 파일에서 추출된 세션 단위 타임아웃 주입 적용
      session.connect(sessionTimeOut * 1000);

      // SFTP 서브 전송 채널 오픈
      channel = session.openChannel("sftp");
      channel.connect(connectTimeOut * 1000);

      // 전송 전용 채널 인스턴스 캐스팅 처리
      channelSftp = (ChannelSftp) channel;

    } catch (JSchException e) {
      log.error("[❌ SFTP 커넥션 실패] 호스트: {}, 사유: {}", host, e.getMessage());
      return false;
    }

    return true;
  }

  /**
   * SFTP 물리 커넥션 자원을 안전 패러다임에 맞춰 폐쇄 및 자원 반납합니다.
   */
  public void close() {
    try {
      if (channelSftp != null && channelSftp.isConnected()) {
        channelSftp.disconnect();
      }
      if (channel != null && channel.isConnected()) {
        channel.disconnect();
      }
      if (session != null && session.isConnected()) {
        session.disconnect();
      }
    } catch (Exception e) {
      log.error("[⚠️ SFTP 자원 해제 중 예외 발생] : {}", e.getMessage());
    }
  }

  /**
   * 레이더 사이트 원격 경로 내 관측 데이터 파일 실시간 유무 판단
   */
  @SuppressWarnings("rawtypes")
  public boolean fileExists(String path, String fileName, String siteCd, String dataKind, String filePattern, String timeZone) {
    log.info("[📂 {} 탐색] 경로: {}/{}", siteCd, path, fileName);

    Vector res = null;
    try {
      res = channelSftp.ls(path + "/" + fileName);
    } catch (SftpException e) {
      if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
        return false;
      }
      log.error("[❌ ls 명령어 에러] 지점: {}, 원인코드: {}", siteCd, e.id);
    }
    return res != null && !res.isEmpty();
  }

  /**
   * 파일 품질 수신 정상성 체크를 위한 용량 크기(Size) 획득
   */
  public Long fileSize(String path, String fileName, Long file_size_min, Long file_size_max) {
    Long fileSize = 0L;
    try {
      fileSize = channelSftp.lstat(path + "/" + fileName).getSize();
    } catch (SftpException e) {
      if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
        return 0L;
      }
      log.error("[❌ lstat 파일 정보 획득 에러] 파일명: {}, 원인: {}", fileName, e.getMessage());
    }
    return fileSize;
  }
}
