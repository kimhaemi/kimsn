"""/* Claude 주석 (2026-11-19) */ 새로 작성한 파일 — Spring Boot 설정 키 정적 검사.

사용법: python3 -I check_props.py <boot.jar> <yml> [<yml> ...]

- boot.jar 안 BOOT-INF/lib/*.jar 의 META-INF/(additional-)spring-configuration-metadata.json 을 모아
  알려진 속성 / deprecated(대체 키) / 삭제(error) 목록을 만든다.
- yml 의 모든 leaf 키를 펼쳐서 spring./server./logging. 접두어 키를 메타데이터와 대조한다.
- 값은 출력하지 않는다 (비밀번호 노출 방지).
"""
import io
import json
import sys
import zipfile

import yaml

CHECK_PREFIXES = ("spring.", "server.", "logging.", "management.")


def load_metadata(boot_jar):
    props, groups, deprecated = {}, set(), {}
    with zipfile.ZipFile(boot_jar) as outer:
        for name in outer.namelist():
            if not (name.startswith("BOOT-INF/lib/") and name.endswith(".jar")):
                continue
            with zipfile.ZipFile(io.BytesIO(outer.read(name))) as inner:
                for meta in ("META-INF/spring-configuration-metadata.json",
                             "META-INF/additional-spring-configuration-metadata.json"):
                    if meta not in inner.namelist():
                        continue
                    data = json.loads(inner.read(meta))
                    for g in data.get("groups", []):
                        groups.add(g["name"])
                    for p in data.get("properties", []):
                        props[p["name"]] = p
                        if "deprecation" in p:
                            deprecated[p["name"]] = p["deprecation"]
    return props, groups, deprecated


def flatten(node, prefix=""):
    if isinstance(node, dict):
        for k, v in node.items():
            key = str(k).strip("[]'\"")
            yield from flatten(v, f"{prefix}.{key}" if prefix else key)
    else:
        yield prefix


def is_known(key, props, groups):
    if key in props:
        return True
    # map 타입 속성 (예: logging.level.<package>, spring.jpa.properties.<any>)
    parts = key.split(".")
    for i in range(len(parts) - 1, 0, -1):
        parent = ".".join(parts[:i])
        p = props.get(parent)
        if p and str(p.get("type", "")).startswith("java.util.Map"):
            return True
    return False


def main():
    boot_jar, ymls = sys.argv[1], sys.argv[2:]
    props, groups, deprecated = load_metadata(boot_jar)
    print(f"[metadata] {boot_jar.rsplit('/', 1)[-1]}: properties={len(props)}, deprecated/removed={len(deprecated)}")
    for y in ymls:
        with open(y, encoding="utf-8") as f:
            docs = [d for d in yaml.safe_load_all(f) if d]
        keys = sorted({k for d in docs for k in flatten(d)})
        checked = [k for k in keys if k.startswith(CHECK_PREFIXES)]
        print(f"\n== {y}  (leaf keys={len(keys)}, spring/server/logging keys={len(checked)})")
        for k in checked:
            if k in deprecated:
                d = deprecated[k]
                print(f"  DEPRECATED[{d.get('level', 'warning')}] {k} -> {d.get('replacement', '(대체 없음)')}  {d.get('reason', '')}")
            elif not is_known(k, props, groups):
                print(f"  UNKNOWN  {k}")
            else:
                print(f"  ok       {k}")
        other = [k for k in keys if not k.startswith(CHECK_PREFIXES)]
        if other:
            tops = sorted({k.split(".")[0] for k in other})
            print(f"  (검사 제외: 커스텀 접두어 {tops}, 키 {len(other)}개)")


if __name__ == "__main__":
    main()
