package kr.or.kimsn.radarsms.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radarsms.dto.StationDto;

public interface StationRepository extends JpaRepository<StationDto, String> {

    List<StationDto> findByOrderBySortOrder();

    StationDto findBySiteCdOrderBySortOrder(String siteCd);

}
