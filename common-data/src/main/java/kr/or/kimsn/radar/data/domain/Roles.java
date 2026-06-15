package kr.or.kimsn.radar.data.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum Roles {
    USER("USER"),
    ADMIN("ADMIN");
    
    private String value;
}
