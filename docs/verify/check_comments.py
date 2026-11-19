"""/* Claude 주석 (2026-11-19) */ 새로 작성한 파일 — 주석 구분 검사.

사용법 (프로젝트 루트에서):  python3 -I docs/verify/check_comments.py [기준_commit]
  기준 commit 기본값: b250e18 (업그레이드 작업 시작 전 마지막 commit)

검사 1) 기준 이후 "추가된 주석" 중 "Claude 주석" 표시가 없는 줄
        - 새로 작성한 파일, "/* Claude 주석 (...) 시작: ~ /* Claude 주석 (...) 끝" 블록 내부는 제외
        - 표시된 줄 바로 다음의 들여쓴 연속 줄(여러 줄 주석의 2번째 줄 이후)은 '연속'으로 따로 표시
        - 기존 주석을 원문 그대로 복원한 줄은 검사 2와 함께 보면 구분됨
검사 2) 기준 commit에 있던 주석 중 현재 파일에서 사라진 줄 (0건이어야 함)
"""
import re
import subprocess
import sys

TAG = "Claude 주석"
BASE = sys.argv[1] if len(sys.argv) > 1 else "b250e18"
EXCLUDE = [":!docs", ":!gradle/wrapper", ":!gradlew", ":!gradlew.bat", ":!.gitignore"]
COMMENT = re.compile(r"\s*(//|#|<!--|\*|/\*)")
TRAILING = re.compile(r"\s(//|#|/\*)\s")


def diff_lines():
    out = subprocess.run(["git", "diff", "-U0", BASE, "--", "."] + EXCLUDE,
                         capture_output=True, text=True, check=True).stdout
    f = None
    for line in out.splitlines():
        if line.startswith("+++ "):
            f = line[6:] if line != "+++ /dev/null" else None
        elif line.startswith("--- "):
            continue
        elif line[:1] in "+-" and f:
            yield f, line[0], line[1:]


def new_files():
    out = subprocess.run(["git", "diff", "--name-only", "--diff-filter=A", BASE, "--", "."] + EXCLUDE,
                         capture_output=True, text=True, check=True).stdout
    return set(out.split())


def main():
    added_new = new_files()
    untagged, continuation = [], []
    inblock, prev_tagged, cur = False, False, None
    for f, sign, s in diff_lines():
        if f != cur:
            cur, inblock, prev_tagged = f, False, False
        if sign != "+" or f in added_new:
            continue
        if TAG in s and ") 시작:" in s:
            inblock = True
        if TAG in s and ") 끝" in s:
            inblock = False
            continue
        is_comment = COMMENT.match(s) or TRAILING.search(s)
        if not is_comment or inblock:
            prev_tagged = False
            continue
        if TAG in s:
            prev_tagged = True
            continue
        if re.match(r"\s*(\*/|/\*\*?|\*)\s*$", s):
            continue
        (continuation if prev_tagged else untagged).append(f"{f}: {s.strip()[:140]}")

    missing = []
    for f, sign, s in diff_lines():
        if sign != "-" or re.match(r"\s*(import|package)", s):
            continue
        if not (re.match(r"\s*(//|#|<!--)", s) or re.search(r"\s//\s", s)):
            continue
        core = re.split(r"//|#|<!--", s, maxsplit=1)[1].strip()
        try:
            cur_text = open(f, encoding="utf-8").read()
        except FileNotFoundError:
            cur_text = ""
        if core and core not in cur_text:
            missing.append(f"{f}: {s.strip()[:140]}")

    print(f"기준 commit: {BASE}, 새로 작성한 파일 {len(added_new)}개는 검사 1에서 제외")
    print(f"\n[검사 1-a] 표시 없는 추가 주석 (기존 주석 복원 줄일 수 있음 — git show {BASE}:<파일> 로 원문 확인): {len(untagged)}건")
    print("\n".join("  " + x for x in untagged))
    print(f"\n[검사 1-b] 표시된 주석의 연속 줄 (정상): {len(continuation)}건")
    print(f"\n[검사 2] 사라진 기존 주석 (0건이어야 함): {len(missing)}건")
    print("\n".join("  " + x for x in missing))
    sys.exit(1 if missing else 0)


if __name__ == "__main__":
    main()
