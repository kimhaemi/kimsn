-- 자료 수신 확인 이력 index 추가
ALTER TABLE watchdog.receive_data ADD INDEX idx_recv_site_time (site, data_time, recv_condition);

commit;