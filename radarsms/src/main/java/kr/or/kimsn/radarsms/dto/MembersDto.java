package kr.or.kimsn.radarsms.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.crypto.password.PasswordEncoder;

import kr.or.kimsn.radarsms.domain.Roles;
import lombok.Data;

@Data
@Entity
@Table(name = "watchdog_member")
// @ToString(exclude = "password")
public class MembersDto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid") private Long id;
  
    @Column(name = "member_id")       private String memberId;
    @Column(name = "password")        private String password;
    @Column(name = "password_hint")   private String passwordHint;
    @Column(name = "password_answer") private String passwordAnswer;
    @Column(name = "member_name")     private String memberName;
    @Column(name = "member_email")    private String memberEmail;
    @Column(name = "member_phone")    private String memberPhone;
    @Column(name = "member_class")    private String memberClass;
    @Column(name = "member_org")      private String memberOrg;
    @Column(name = "member_dept")     private String memberDept;
    @Column(name = "member_pos")      private String memberPos;
    
   //  @UpdateTimestamp
    @Column(name = "member_join")
    private Date memberJoin;

    @Enumerated(EnumType.STRING)
    private Roles roles;

    /**
    * 
    * @description 패스워드 저장시 자동 암호화
    */
   public MembersDto encodePassword(PasswordEncoder passwordEncoder) {
      this.password = passwordEncoder.encode(this.password);
      return this;
   }
  
}
