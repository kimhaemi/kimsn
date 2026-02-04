package kr.or.kimsn.radarsms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import kr.or.kimsn.radarsms.dto.AppTemplateCodeDto;

public interface AppTemplateCodeRepository extends JpaRepository<AppTemplateCodeDto, String> {

    @Query(nativeQuery = true, value = "INSERT INTO nuri.app_template_code \n" +
            " (APP_GUBUN, TEMPLATE_CODE, USE_BUTTON, HEAD, FOOT, TITLE)\n" +
            "VALUES ( \n" +
            " 'KAKAO',  \n" +
            " :templateCode, \n" +
            " 'N', \n" +
            " :head, \n" +
            " NULL, \n" +
            " NULL\n" +
            ")")
    @Transactional
    @Modifying
    // 템플릿 등록
    Integer setAppTemplateCodeAdd(
            @Param("templateCode") String templateCode,
            @Param("head") String head);

    @Query(nativeQuery = true, value = "update nuri.app_template_code set \n" +
            "      TEMPLATE_CODE = :newTemplateCode \n" +
            "    , HEAD = :head \n" +
            "    , USE_BUTTON = :useButton \n" +
            "where 1=1\n" +
            "  and TEMPLATE_CODE = :oldTemplateCode \n")
    @Transactional
    @Modifying
    // 템플릿 수정
    Integer setAppTemplateCodeModify(
            @Param("newTemplateCode") String newTemplateCode,
            @Param("oldTemplateCode") String oldTemplateCode,
            @Param("head") String head,
            @Param("useButton") String useButton);

    @Query(nativeQuery = true, value = "delete from nuri.app_template_code \n" +
            "where 1=1\n" +
            "  and TEMPLATE_CODE = :templateCode \n")
    @Transactional
    @Modifying
    // 템플릿 삭제
    Integer setAppTemplateCodeDelete(
            @Param("templateCode") String templateCode);

}
