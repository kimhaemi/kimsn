package kr.or.kimsn.radar.daemon.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import kr.or.kimsn.radar.data.dto.AppTemplateCodeDto;
import kr.or.kimsn.radar.data.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radar.data.dto.ReceiveConditionDto;
import kr.or.kimsn.radar.data.dto.ReceiveDataDto;
import kr.or.kimsn.radar.data.dto.ReceiveSettingDto;
import kr.or.kimsn.radar.data.dto.SmsSendMemberDto;
import kr.or.kimsn.radar.data.dto.SmsSendOnOffDto;
import kr.or.kimsn.radar.data.dto.SmsSendPatternDto;
import kr.or.kimsn.radar.data.dto.StationDto;
import kr.or.kimsn.radar.data.dto.StationStatusDto;
import kr.or.kimsn.radar.data.repository.ReceiveConditionCriteriaRepository;
import kr.or.kimsn.radar.data.repository.ReceiveConditionRepository;
import kr.or.kimsn.radar.data.repository.ReceiveDataRepository;
import kr.or.kimsn.radar.data.repository.ReceiveSettingRepository;
import kr.or.kimsn.radar.data.repository.SmsSendMemberRepositroy;
import kr.or.kimsn.radar.data.repository.SmsSendOnOffRepository;
import kr.or.kimsn.radar.data.repository.SmsSendPatternRepository;
import kr.or.kimsn.radar.data.repository.SmsSendRepository;
import kr.or.kimsn.radar.data.repository.StationRepository;
import kr.or.kimsn.radar.data.repository.StationStatusRepository;
import kr.or.kimsn.radar.data.repository.SmsSendNuri2Repository;
import kr.or.kimsn.radar.data.repository.AppTemplateCodeRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryService {

    private final StationRepository stationRepository; // site
    private final ReceiveSettingRepository receiveSettingRepository;
    private final SmsSendOnOffRepository smsSendOnOffRepository;
    private final ReceiveConditionCriteriaRepository receiveConditionCriteriaRepository;// 경고 기준
    private final SmsSendPatternRepository smsSendPatternRepository;// 문자메시지 패턴
    private final SmsSendRepository smsSendRepository; // 문자 메시지 전송(app_send_data, app_send_contents)

    private final SmsSendNuri2Repository smsSendNuri2Repository; // 문자 메시지 전송(NURI2_NRMSG_DATA)
    private final SmsSendMemberRepositroy smsSendMemberRepositroy; // 수신자 그룹

    private final StationStatusRepository stationStatusRepository; // 지점별 운영상태

    private final ReceiveConditionRepository receiveConditionRepository; // 최종
    private final ReceiveDataRepository receiveDataRepository; // 이력

    private final AppTemplateCodeRepository appTemplateCodeRepository; //템플릿

    // site 조회
    public List<StationDto> getStation(int gubun, String agencyCd, int status) {
        // return stationRepository.findByGubunOrderBySortOrder(gubun);
        return stationRepository.findByGubunAndAgencyCdAndStatusOrderBySortOrder(gubun, agencyCd, status);
    }

    // 자료 수신 처리 설정 조회
    public ReceiveSettingDto getrRceiveSetting(String dataKindStr) {
        return receiveSettingRepository.findByDataKindAndPermittedWatchAndStatus(dataKindStr, 1, 1);
    }

    // 최종 처리 상태
    public List<ReceiveConditionDto> getReceiveConditionList(String dataKindStr, String data_type) {
        return receiveConditionRepository.findByDataKindAndDataType(dataKindStr, data_type);
    }

    public List<ReceiveConditionDto> getReceiveConditionList(String dataKindStr, String data_type, String agencyCd) {
        return receiveConditionRepository.findByDataKindAndDataTypeAndAgencyCd(dataKindStr, data_type, agencyCd);
    }

    // 최종 처리 상태 - site 별
    public ReceiveConditionDto getReceiveCondition(String dataKindStr, String data_type, String site_cd) {
        return receiveConditionRepository.findByDataKindAndDataTypeAndSite(dataKindStr, data_type, site_cd);
    }

    // 경고 기준 (횟수 - criterion) - List
    public List<ReceiveConditionCriteriaDto> getReceiveConditionCriteriaList(int gubun) {
        return receiveConditionCriteriaRepository.findByGubunOrderBySort(gubun);
    }

    // 경고 기준 (횟수 - criterion) - 단건
    public ReceiveConditionCriteriaDto getReceiveConditionCriteria(int gubun, String code, String codedtl) {
        return receiveConditionCriteriaRepository.getReceiveConditionCriteria(gubun, code, codedtl);
    }

    // 문자메시지 패턴
    public List<SmsSendPatternDto> getSmsSendPattern(int activation, int status, String code, String codedtl) {
        // return
        // smsSendPatternRepository.findByActivationAndStatusAndModeAndCodeAndCodedtl(activation,
        // status, mode, code, codedtl);
        return smsSendPatternRepository.findByActivationAndStatusAndCodeAndCodedtl(activation, status, code, codedtl);
    }

    // data 처리 이력
    public List<ReceiveDataDto> getReceiveDataList(String site, String dataKindStr, int count) {
        // log.info("receiveDataRepository.getReceiveDataListLimit(site, dataKindStr, count): {}", receiveDataRepository.getReceiveDataListLimit(site, dataKindStr, count));
        return receiveDataRepository.getReceiveDataListLimit(site, dataKindStr, count);
    }

    // 문자 메시지 on/off
    public SmsSendOnOffDto getSmsSendOnOffData() {
        return smsSendOnOffRepository.findByCode("STOPSMS");
    }

    // data 처리 이력 - dtl
    public List<ReceiveDataDto> getReceiveDataCodedtlList(String site, String dataKindStr, int count, String codedtl) {
        return receiveDataRepository.getReceiveDataCodedtlList(site, dataKindStr, codedtl, PageRequest.of(0, count));
    }

    // site 수신그룹 담당자
    public List<SmsSendMemberDto> getSmsSendMemberList(String data_kind, String site) {
        return smsSendMemberRepositroy.getSmsSendMemberList(data_kind, site);
    }

    // app sequence
    public Long getAppContentNextval() {
        return smsSendRepository.getAppContentNextval();
    }

    // 최종결과 문자발송 update
    public Integer updateReceiveConditionSms(int sms_send, String where_recv_condition, String site, String dataKindStr,
            String dataType) {
        return receiveConditionRepository.updateReceiveConditionSms(sms_send, where_recv_condition, site, dataKindStr,
                dataType);
    }

    // 최종 결과 update query
    public Integer updateReceiveCondition(String apply_time, String new_recv_condition, String new_codedtl,
            int sms_send, String where_recv_condition, String site, String dataKindStr, String dataType) {
        // log.info("===== updateReceiveCondition start ====");
        // log.info("apply_time: " + apply_time);
        // log.info("new_recv_condition: " + new_recv_condition);
        // log.info("new_codedtl: " + new_codedtl);
        // log.info("sms_send: " + sms_send);
        // log.info("where_recv_condition: " + where_recv_condition);
        // log.info("site: " + site);
        // log.info("dataKindStr: " + dataKindStr);
        // log.info("dataType: " + dataType);
        // log.info("===== updateReceiveCondition end ====");
        Integer updateResult = receiveConditionRepository.updateReceiveCondition(apply_time, new_recv_condition, new_codedtl, sms_send,
                where_recv_condition, site, dataKindStr, dataType);
        log.info("[{} 최종상태 update]: {}", site, updateResult == 1? "성공":"실패");
        return updateResult;
        // select * from receive_condition rc where recv_condition = 'TOTA' and site =
        // 'TEST' and data_kind = 'RDR' and data_type = 'NQC';
    }

    // 이력 update
    public Integer updateReceiveData(String new_recv_condition, String new_codedtl, String site, String dataKindStr,
            String dataType, String where_recv_condition, String data_kst) {

                // log.info("================== updateReceiveData start ===================");
                // log.info("new_recv_condition: {}", new_recv_condition);
                // log.info("new_codedtl: {}", new_codedtl);
                // log.info("site: {}", site);
                // log.info("dataKindStr: {}", dataKindStr);
                // log.info("dataType: {}", dataType);
                // log.info("where_recv_condition: {}", where_recv_condition);
                // log.info("data_kst: {}", data_kst);
                // log.info("================== updateReceiveData end ===================");
        // 🔥 리턴값을 변수에 저장
        Integer updatedRows = receiveDataRepository.updateReceiveData(new_recv_condition,
                new_codedtl, site, dataKindStr, dataType, where_recv_condition, data_kst);
                
        log.info("🔥 [{} receive data UPDATE 결과]: {}", site, updatedRows);
        
        return updatedRows;
    }

    public void insReceiveData(String dataKindStr, String site_cd, String dataType, String data_time, String data_kst,
            String recv_time,
            String recv_condition, String recv_condition_check_time, String file_name, Long file_size, String codedtl) {
        ReceiveDataDto rdDto = new ReceiveDataDto();

        // log.info("[==InsReceiveData==]");
        // System.out.println("data_kind : " + dataKindStr);
        // System.out.println("site : " + site_cd);
        // System.out.println("data_type : " + dataType);
        // System.out.println("data_time : " + data_time);
        // System.out.println("data_kst : " + data_kst);
        // System.out.println("recv_time : " + recv_time);
        // System.out.println("recv_condition : " + recv_condition);
        // System.out.println("recv_condition_check_time : " +
        // recv_condition_check_time);
        // System.out.println("file_name : " + file_name);
        // System.out.println("file_size : " + file_size);
        // System.out.println("codedtl : " + codedtl);

        rdDto.setData_kind(dataKindStr);
        rdDto.setSite(site_cd);
        rdDto.setData_type(dataType);
        rdDto.setData_time(data_time);
        rdDto.setData_kst(data_kst);
        rdDto.setRecv_time(recv_time);
        rdDto.setRecv_condition(recv_condition);
        rdDto.setRecv_condition_check_time(recv_condition_check_time);
        rdDto.setFile_name(file_size == 0 ? "" : file_name);
        rdDto.setFile_size(file_size);
        rdDto.setCodedtl(codedtl);

        // receiveDataRepository.save(rdDto);
        ReceiveDataDto result = receiveDataRepository.saveAndFlush(rdDto);
        log.info("[{} receive_data insert 결과]: {}",site_cd, result);
    }

    // 문자 전송(NURI2_NRMSG_DATA) insert
    public void intNuri2Save(String resDate, String callTo, String callFrom, String templateCode, String smsTitle, String titleAndText) {
        smsSendNuri2Repository.nuri2SendContentsSave(resDate, callTo, callFrom, templateCode, smsTitle, titleAndText);
    }

    // 특정 시간 구하기
    public String getPreviousTime(int second) {
        return receiveDataRepository.getPreviousTime(second);
    }

    // 지점별 운영상태
    public List<StationStatusDto> getStationStatusGubun(int gubun) {
        return stationStatusRepository.findByGubun(gubun);
    }

    public List<StationStatusDto> findByGubunAndAgencyCd(int gubun, String agencyCd) {
        return stationStatusRepository.findByGubunAndAgencyCd(gubun, agencyCd);
    }

    // 지점별 운영상태
    public StationStatusDto getStationStatus(String sitecd) {
        return stationStatusRepository.findBySiteCd(sitecd);
    }

    public AppTemplateCodeDto getTemplateCode(String templateCode) {
        return appTemplateCodeRepository.findByTemplateCode(templateCode);
    }
}
