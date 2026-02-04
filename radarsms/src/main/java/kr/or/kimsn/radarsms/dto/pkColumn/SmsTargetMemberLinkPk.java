package kr.or.kimsn.radarsms.dto.pkColumn;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsTargetMemberLinkPk implements Serializable{
    private Long gid;
    private Long id;
}
