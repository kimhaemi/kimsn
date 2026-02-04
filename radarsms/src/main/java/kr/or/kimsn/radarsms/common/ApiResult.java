package kr.or.kimsn.radarsms.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collection;
import lombok.Getter;
import org.springframework.lang.Nullable;

/**
 * RestController 에서 api 에 대한 공통 return type class.
 * aop 에서 리턴 타입 체크를 하므로 패키지 경로등을 옮길 시 주의 필요.
 * ApiResult<Type> 형태로 리턴 타입을 되도록 명시해서 사용할 것.
 *
 * success - 성공시 사용
 * fail - 내부 로직 처리중 실패시 사용. 실패사유를 결과 타입 부분과 일치시켜 주기 쉽지 않으므로 그냥 error 사용을 권장
 * error - 서버 에러등이 발생했을 사용이지만 fail 을 대신해 사용하면 되며, throw Exception 을 발생시키면 자동으로 error 로 리턴함
 */

@Getter
public class ApiResult<T> {

  public ApiResult() {
    throw new UnsupportedOperationException();
  }

  private final StatusEnum status;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private final String message;

  private final T data;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private final String code; // optional when error occurs

  public ApiResult(StatusEnum status, String message, T data, String code) {
    this.status = status;
    this.message = message;
    this.data = data;
    this.code = code;
  }

  public static <T> ApiResult<T> success(@Nullable T data) {
    return new ApiResult<>(StatusEnum.SUCCESS, null, data, null);
  }

  public static <T> ApiResult<T> error(String message, T causeData, @Nullable String code) {
    return new ApiResult<>(StatusEnum.ERROR, message, causeData, code);
  }

  public int getDataSize() {
    if (data instanceof Collection) {
      return ((Collection<?>) data).size();
    }else {
      return 1;
    }
  }

  private enum StatusEnum {
    SUCCESS("success"), FAIL("fail"), ERROR("error");

    private final String status;

    StatusEnum(final String status) {
      this.status = status;
    }

    public String getStatus() {
      return status;
    }

    @JsonValue
    @Override
    public String toString() {
      return this.status.toLowerCase();
    }

  }

}
