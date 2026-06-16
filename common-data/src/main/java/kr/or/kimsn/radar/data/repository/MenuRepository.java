package kr.or.kimsn.radar.data.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radar.data.dto.MenuDto;

public interface MenuRepository extends JpaRepository<MenuDto, Long> {
  List<MenuDto> findByStatusOrderByOrder(Boolean status);
}
