[설정 레이어]
├ siteInfoSetting.conf / siteIPInfoSetting.conf (장비 경로, 타임아웃, 접속 계정 정보)
└ application.yml (로그 패턴 및 등급 제어)
│
▼
[스케줄러 레이어]
└ Scheduler.java (중복 방지 락 처리, 쓰레드풀 병렬 실행 제어, 35초 대기선)
│
├─▶ [Step 1: 수집 레이어] StepOneProcess.java
│    └ SftpUtil을 활용한 파일 존재 및 크기 정밀 실시간 체크 (RECV / MISS 적재)
│
└─▶ [Step 2: 정산 레이어] StepTwoProcess.java
└ 연속 장애/복구 매트릭스 필터 검증 ➔ 최종 상태 갱신
│
▼
[Step 3: 알림 레이어] SmsNotificationService.java
└ 일괄 장애(TOTA)/복구(TORE) 및 지점 단독 장애 문자 포맷 큐 인서트