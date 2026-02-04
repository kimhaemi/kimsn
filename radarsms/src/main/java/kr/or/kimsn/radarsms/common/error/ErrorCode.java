package kr.or.kimsn.radarsms.common.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

/**
 * 각 서비스에서 비즈니스 오류 발생시킬 경우 이곳에 에러코드를 정의하고 BusinessException 을 throw
 * 정의한 코드값에 대한 메시지는 messages 에서 한글/영문으로 정의 필요
 */

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

  // Default - code 값 없을시 name = message
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ""),
  BAD_REQUEST(HttpStatus.BAD_REQUEST, ""),
  FORBIDDEN(HttpStatus.FORBIDDEN, ""),
  NOT_FOUND(HttpStatus.NOT_FOUND, ""),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ""),
  NO_CONTENT(HttpStatus.NO_CONTENT, ""),
  ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, ""),
  NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, ""),
  CONFLICT(HttpStatus.CONFLICT, ""),

  // Common
  INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "C000"),
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001"),
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C002"),
  ILLEGAL_ARGUMENT_GIVEN(HttpStatus.BAD_REQUEST, "C003"),
  AUTHENTICATION_DENIED(HttpStatus.UNAUTHORIZED, "C004"),
  CONTRACT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005"),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C006"),
  INVALID_REGEX_PATTERN(HttpStatus.BAD_REQUEST, "C007"),
  CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "C008"),
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "C009"),
  FILE_STORE_INIT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C010"),
  INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "C011"),
  FILE_STORE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "C012"),
  PRIVATE_RESOURCE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C013"),
  ADMIN_PERMISSION_REQUIRED(HttpStatus.FORBIDDEN, "C014"),
  API_CALL_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "C015"),
  FREEMARKER_TEMPLATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C016"),
  MAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "C017"),
  CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "C018"),
  HTTP_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C019"),
  INAPPROPRIATE_REQUEST(HttpStatus.BAD_REQUEST, "C020"),
  ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C021"),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "C022"),
  PROCESSED_FAILED(HttpStatus.BAD_REQUEST, "C023"),
  PROTECTED_FLAG_RESOURCE_DENIED(HttpStatus.BAD_REQUEST, "C024"),
  REMOVED_RESOURCE(HttpStatus.BAD_REQUEST, "C025"),
  ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "C026"),
  SYSTEM_ADMIN_PERMISSION_REQUIRED(HttpStatus.FORBIDDEN, "C027"),
  PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "C999"),
  DUPLICATED_ACTION(HttpStatus.NOT_ACCEPTABLE, "C900"),

  // Auth (Sample)
  USER_NOT_FOUND(HttpStatus.NOT_ACCEPTABLE, "A019"),
  ROLE_NOT_FOUND(HttpStatus.NOT_ACCEPTABLE, "A020"),

  // billing
  NEED_MORE_CREDIT(HttpStatus.FORBIDDEN, "B001"),
  DATA_TO_CLOSING_NOT_FOUND(HttpStatus.BAD_REQUEST, "B002"),
  DATA_TO_READING_NOT_FOUND(HttpStatus.BAD_REQUEST, "B003"),
  DATA_TO_INVOCING_NOT_FOUND(HttpStatus.BAD_REQUEST, "B004"),
  DATA_TO_CHARGING_NOT_FOUND(HttpStatus.BAD_REQUEST, "B005"),
  DATA_TO_RATING_NOT_FOUND(HttpStatus.BAD_REQUEST, "B006"),
  DATA_TO_METERING_NOT_FOUND(HttpStatus.BAD_REQUEST, "B007"),
  DATA_TO_CLOSING_FOUND(HttpStatus.BAD_REQUEST, "B008"),
  DATA_TO_READING_FOUND(HttpStatus.BAD_REQUEST, "B009"),
  ADJUST_AMT_IS_ZERO(HttpStatus.BAD_REQUEST, "B010"),
  DATA_TO_DEADLINE_NOT_FOUND(HttpStatus.BAD_REQUEST, "B011"),
  DATA_TO_INVOCING_CLOSE_NOT_FOUND(HttpStatus.BAD_REQUEST, "B012"),
  DATA_TO_EXTERNAL_METERING_IS_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "B013"),

  D_PLUS_RESPONSE_FAIL(HttpStatus.BAD_REQUEST, ""),

  ;



  private HttpStatus status;
  private final String code;

  ErrorCode(final HttpStatus status, final String code) {
    this.status = status;
    this.code = code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

}
