package kr.or.kimsn.radardemo.service;

import java.util.List;

import kr.or.kimsn.radardemo.dto.repository.SmsSendNuri2Repository;
import org.springframework.stereotype.Service;

import kr.or.kimsn.radardemo.dto.ReceiveConditionCriteriaDto;
import kr.or.kimsn.radardemo.dto.ReceiveConditionDto;
import kr.or.kimsn.radardemo.dto.ReceiveDataDto;
import kr.or.kimsn.radardemo.dto.ReceiveSettingDto;
import kr.or.kimsn.radardemo.dto.SmsSendMemberDto;
import kr.or.kimsn.radardemo.dto.SmsSendOnOffDto;
import kr.or.kimsn.radardemo.dto.SmsSendPatternDto;
import kr.or.kimsn.radardemo.dto.StationDto;
import kr.or.kimsn.radardemo.dto.StationStatusDto;
import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionCriteriaRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveConditionRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveDataRepository;
import kr.or.kimsn.radardemo.dto.repository.ReceiveSettingRepository;
import kr.or.kimsn.radardemo.dto.repository.SmsSendMemberRepositroy;
import kr.or.kimsn.radardemo.dto.repository.SmsSendOnOffRepository;
import kr.or.kimsn.radardemo.dto.repository.SmsSendPatternRepository;
import kr.or.kimsn.radardemo.dto.repository.SmsSendRepository;
import kr.or.kimsn.radardemo.dto.repository.StationRepository;
import kr.or.kimsn.radardemo.dto.repository.StationStatusRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
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

    // site 조회
    public List<StationDto> getStation(int gubun) {
        return stationRepository.findByGubunOrderBySortOrder(gubun);
    }

    // 자료 수신 처리 설정 조회
    public ReceiveSettingDto getrRceiveSetting(String dataKindStr) {
        return receiveSettingRepository.findByDataKindAndPermittedWatchAndStatus(dataKindStr, 1, 1);
    }

    // 최종 처리 상태
    public List<ReceiveConditionDto> getReceiveConditionList(String dataKindStr, String data_type) {
        return receiveConditionRepository.findByDataKindAndDataType(dataKindStr, data_type);
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
        return receiveDataRepository.getReceiveDataList(site, dataKindStr, count);
    }

    // 문자 메시지 on/off
    public SmsSendOnOffDto getSmsSendOnOffData() {
        return smsSendOnOffRepository.findByCode("STOPSMS");
    }

    // data 처리 이력 - dtl
    public List<ReceiveDataDto> getReceiveDataCodedtlList(String site, String dataKindStr, int count, String codedtl) {
        return receiveDataRepository.getReceiveDataCodedtlList(site, dataKindStr, count, codedtl);
    }

    // site 수신그룹 담당자
    public List<SmsSendMemberDto> getSmsSendMemberList(String data_kind, String site) {
        return smsSendMemberRepositroy.getSmsSendMemberList(data_kind, site);
    }

    // app sequence
    public Long getAppContentNextval() {
        return Long.parseLong(smsSendRepository.getAppContentNextval());
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
        return receiveConditionRepository.updateReceiveCondition(apply_time, new_recv_condition, new_codedtl, sms_send,
                where_recv_condition, site, dataKindStr, dataType);
        // select * from receive_condition rc where recv_condition = 'TOTA' and site =
        // 'TEST' and data_kind = 'RDR' and data_type = 'NQC';
    }

    // 이력 update
    public Integer updateReceiveData(String new_recv_condition, String new_codedtl, String site, String dataKindStr,
            String dataType, String where_recv_condition, String data_kst) {
        return receiveDataRepository.updateReceiveData(new_recv_condition,
                new_codedtl, site, dataKindStr, dataType, where_recv_condition, data_kst);
    }

    // 최종 결과 update
    public void insReceiveCondition(String site_cd, String dataKindStr, String dataType, String recv_condition,
            String apply_time, String last_check_time, int sms_send, int sms_send_activation, int status,
            String codedtl) {
        ReceiveConditionDto rcDto = new ReceiveConditionDto();

        System.out.println("[==updateReceiveCondition==]");
        // System.out.println(" site : " + site_cd);
        // System.out.println(" data_kind : " + dataKindStr);
        // System.out.println(" data_type : " + dataType);
        // System.out.println(" recv_condition : " + recv_condition);
        // System.out.println(" apply_time : " + apply_time);
        // System.out.println(" last_check_time : " + last_check_time);
        // System.out.println(" sms_send : " + sms_send);
        // System.out.println(" sms_send_activation : " + sms_send_activation);
        // System.out.println(" status : " + status);
        // System.out.println(" codedtl : " + codedtl);

        rcDto.setSite(site_cd);
        rcDto.setDataKind(dataKindStr);
        rcDto.setDataType(dataType);
        rcDto.setRecv_condition(recv_condition);
        rcDto.setApply_time(apply_time);
        rcDto.setLast_check_time(last_check_time);
        rcDto.setSms_send(sms_send);
        rcDto.setSms_send_activation(sms_send_activation);
        rcDto.setStatus(status);
        rcDto.setCodedtl(codedtl);

        // receiveConditionRepositor/y.save(rcDto);

    }

    // 쓸데가 있겠지...
    public void insReceiveData(String dataKindStr, String site_cd, String dataType, String data_time, String data_kst,
            String recv_time,
            String recv_condition, String recv_condition_check_time, String file_name, Long file_size, String codedtl) {
        ReceiveDataDto rdDto = new ReceiveDataDto();

        System.out.println("[==InsReceiveData==]");
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

        receiveDataRepository.save(rdDto);
    }

    // 문자 전송(app_send_data) insert
    public void insGaonAppSendDataSave(Long appSeq, String call_to, String call_from) {
        smsSendRepository.gaonAppSendDataSave(appSeq, call_to, call_from); // 템플릿 코드 넣어야함.
    }

    // 문자 전송(app_send_contents) insert
    public void intGaonAppSendContentsSave(Long appSeq, String smsPettern) {
        smsSendRepository.gaonAppSendContentsSave(appSeq, smsPettern);
    }

    // 문자 전송(NURI2_NRMSG_DATA) insert
    public void intNuri2Save(String resDate, String callTo, String callFrom, String templateCode, String altJson) {
        smsSendNuri2Repository.nuri2SendContentsSave(resDate, callTo, callFrom, templateCode, altJson);
    }

    // 특정 시간 구하기
    public String getPreviousTime(int second) {
        return receiveDataRepository.getPreviousTime(second);
    }

    // 지점별 운영상태
    public List<StationStatusDto> getStationStatusGubun(int gubun) {
        return stationStatusRepository.findByGubun(gubun);
    }

    // 지점별 운영상태
    public StationStatusDto getStationStatus(String sitecd) {
        return stationStatusRepository.findBySiteCd(sitecd);
    }
}
