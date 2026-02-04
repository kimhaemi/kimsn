package kr.or.kimsn.radarsms.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "menu")
public class MenuDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	@Column(name = "menu_name") private String menuName;
	private String path; 
	private Boolean status; 
	private int depth;
	private String order; 
	@Column(name = "created_at") private String createdAt;
	@Column(name = "updated_at") private String updatedAt;

}
