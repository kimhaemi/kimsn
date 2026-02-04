package kr.or.kimsn.radarsms.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum Roles {
    USER("USER"),
    ADMIN("ADMIN");
    
    private String value;
}
