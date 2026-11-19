package kr.or.kimsn.radar.data.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
