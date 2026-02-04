package kr.or.kimsn.radardemo.dto;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
//@IdClass(SmsSendNuri2Pk.class)
public class SmsSendNuri2Dto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long msg_key; // # '고유번호', 메시지 접수번호(일련번호), 접수시 필수값, 숫자 11자리, 메시지 일련번호, 반드시 고유값, 중복시 발송실패, 시퀀스(시리얼번호사용)\n"

    private String MSG_STATE; // # '메시지 상태 값', 1:입력(전송대기),  3:전송수집중(QUE 수집), 5:전송(결과대기, SMS:24시간, MMS:72시간 대기), 6:처리완료  회신(결과회신), MSG_STATE='6' 성공, 실패 중 결과 확인된 경우
    private String MSG_STATE_NAME;
    private String INPUT_DATE; // # '메시지 입력시간', 실제로 메시지를 입력한 시간
    private String RES_DATE;   // # '메시지 예약일시', 미래시간, 과거시간(3시간이내 입력시간데이터 조회,  실시간 발송처리 함) 가능
    private String RSLT_CODE;  // # '*결과처리 상세코드', 성공 : MSG_STATE='6' AND (RSLT_CODE='0'OR RSLT_CODE='1000'),
    private String RSLT_CODE_NAME;
    private String REST_NET;   // # '*결과처리 통신사', SKT, KT, LGU, KKO=KAKAO
    private String RSLT_TYPE;  // # '*결과처리 된 메시지 유형', XMS(또는 MMS,SMS), ALT, RCS
    private String RSLT_TYPE_NAME;
    private String RSLT_EXPLA; // '결과코드 상세내용'
    private String PHONE;      // # '수신 번호', [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX
    private String MSG_TYPE; // # '발송 타입 1번째'  SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자
    private String CONTENTS_TYPE_1;     // # '메시지 내용에 대한 타입 1번째' SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자
    private String XMS_SUBJECT;// # '메시지 타이틀(LMS/MMS)'
    private String XMS_TEXT;  // # '메시지 본문(SMS/LMS/MMS)'
    private String ALT_JSON;   // # '메시지 본문(ALT,ALI)', JSON 형식, //, 필수 형식 '{"text":"입력 할 메시지 내용"}'
    private String SMS_TEXT;

}
