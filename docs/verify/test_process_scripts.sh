#!/bin/bash
# /* Claude 주석 (2026-11-19): 새로 작성한 파일 — 운영 실행 스크립트(config/*_process.sh) 검증 (6.8-F) */
#
# 하는 일
#   - 임시 폴더에 스크립트를 복사하고 RADAR_HOME만 임시 폴더로 바꿔서 실행 (운영 경로는 건드리지 않음)
#   - 진짜 프로그램 대신 "받은 DAEMON_TYPE / java.version 을 출력하고 30초 뒤 종료"하는 Probe jar 사용
#   - 가짜 JDK(21.0.5, 25.0.5)로 버전 검사가 시작을 막는지 확인
#   - 끝나면 임시 프로세스를 종료하고 임시 폴더 삭제
#
# 사용법 (프로젝트 루트에서):  bash docs/verify/test_process_scripts.sh [JDK25_경로]
#   JDK25_경로 기본값: /usr/lib/jvm/java-25-openjdk-amd64 (Ubuntu). RHEL이면 /usr/lib/jvm/java-25-openjdk 등
# 기대 결과: 모든 줄이 PASS, 마지막 줄 "결과: FAIL 0"

set -u
JDK="${1:-/usr/lib/jvm/java-25-openjdk-amd64}"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
WORK="$(mktemp -d)"
FAIL=0
trap 'pkill -f "$WORK/" 2>/dev/null; rm -rf "$WORK"' EXIT

[ -x "$JDK/bin/java" ] || { echo "JDK 25 경로가 올바르지 않습니다: $JDK"; exit 2; }

# Probe jar
mkdir -p "$WORK/src" && cat > "$WORK/src/Probe.java" <<'EOF'
public class Probe {
  public static void main(String[] a) throws Exception {
    System.out.println("PROBE DAEMON_TYPE=" + System.getProperty("DAEMON_TYPE") + " java.version=" + System.getProperty("java.version"));
    Thread.sleep(30000);
  }
}
EOF
( cd "$WORK/src" && "$JDK/bin/javac" -d . Probe.java && printf 'Main-Class: Probe\n' > m.txt && "$JDK/bin/jar" cfm "$WORK/probe.jar" m.txt Probe.class ) || exit 2

# 가짜 JDK
for v in 21.0.5 25.0.5; do
  mkdir -p "$WORK/fake-$v/bin"
  printf '#!/bin/sh\necho "    java.version = %s" >&2\n' "$v" > "$WORK/fake-$v/bin/java"
  chmod +x "$WORK/fake-$v/bin/java"
done

check() { if [ "$1" = "$2" ]; then echo "  PASS $3"; else echo "  FAIL $3 (기대: $2, 실제: $1)"; FAIL=$((FAIL+1)); fi; }

for f in RDR RDR_B SDR SDR_B TDWR RADAR_WEB; do
  src="$ROOT/config/${f}_process.sh"
  echo "== $f"
  bash -n "$src"; check "$?" "0" "문법 검사 (bash -n)"
  jar=$(grep -m1 '^RADAR_NAME_JAR=' "$src" | cut -d= -f2)
  home=$(grep -m1 '^RADAR_HOME=' "$src" | cut -d= -f2)
  expect_dt=$(grep -m1 '^RADAR_DAEMON_TYPE=' "$src" | sed 's/.*DAEMON_TYPE=//'); expect_dt=${expect_dt:-null}
  mkdir -p "$WORK/$f"; cp "$WORK/probe.jar" "$WORK/$f/$jar"
  sed "s#^RADAR_HOME=$home\$#RADAR_HOME=$WORK/$f#" "$src" > "$WORK/$f.sh"; chmod +x "$WORK/$f.sh"

  JAVA_HOME=/nonexistent/jdk "$WORK/$f.sh" start >/dev/null 2>&1; check "$?" "1" "JAVA_HOME 경로 없음 → 시작 거부"
  JAVA_HOME="$WORK/fake-21.0.5" "$WORK/$f.sh" start >/dev/null 2>&1; check "$?" "1" "Java 21.0.5 → 시작 거부"
  JAVA_HOME="$WORK/fake-25.0.5" "$WORK/$f.sh" start >/dev/null 2>&1; check "$?" "1" "Java 25.0.5 (패치 전) → 시작 거부"
  JAVA_HOME="$JDK" "$WORK/$f.sh" start >/dev/null 2>&1; check "$?" "0" "JDK 25.0.5.1 이상 → 시작"
  sleep 2
  out=$(cat "$WORK/$f/logs/"*.log 2>/dev/null | head -1)
  case "$out" in *"java.version=25."*) check ok ok "Probe가 JDK 25로 실행됨 ($out)";; *) check "$out" "java.version=25.x" "Probe 실행";; esac
  case "$out" in *"DAEMON_TYPE=$expect_dt "*) check ok ok "DAEMON_TYPE 전달 ($expect_dt)";; *) check "$out" "DAEMON_TYPE=$expect_dt" "DAEMON_TYPE 전달";; esac
  pkill -f "$WORK/$f/$jar" 2>/dev/null
done
echo "결과: FAIL $FAIL"
exit $FAIL
