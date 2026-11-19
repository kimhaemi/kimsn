/* Claude 주석 (2026-11-19): 새로 작성한 파일 — JSch 기본 SSH 알고리즘 출력 (6.8-J)
 *
 * 사용법 (프로젝트 루트에서, 외부 접속 없음):
 *   java -cp libs/jsch-0.1.55.jar docs/verify/JschDefaults.java                       # 기존 jsch
 *   java -cp "$(find ~/.gradle/caches -name 'jsch-2.28.8.jar' | head -1)" docs/verify/JschDefaults.java   # 현재 jsch
 * 두 결과를 비교하면 새 버전에서 빠진 알고리즘(ssh-rsa, sha1 키 교환, CBC 등)을 볼 수 있다.
 */
public class JschDefaults {
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.jcraft.jsch.JSch");
        java.lang.reflect.Method m = c.getMethod("getConfig", String.class);
        for (String k : new String[] { "kex", "server_host_key", "PubkeyAcceptedAlgorithms", "cipher.c2s", "mac.c2s" }) {
            Object v;
            try { v = m.invoke(null, k); } catch (Exception e) { v = "(없음)"; }
            System.out.println(k + " = " + v);
        }
    }
}
