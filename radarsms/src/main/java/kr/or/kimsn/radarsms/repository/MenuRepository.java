package kr.or.kimsn.radarsms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radarsms.dto.MenuDto;

public interface MenuRepository extends JpaRepository<MenuDto, Long> {

}
