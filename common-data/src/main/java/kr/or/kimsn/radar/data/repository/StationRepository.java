package kr.or.kimsn.radar.data.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radar.data.dto.StationDto;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends JpaRepository<StationDto, String> {

    List<StationDto> findByOrderBySortOrder();

    StationDto findBySiteCdOrderBySortOrder(String siteCd);

    List<StationDto> findByGubunOrderBySortOrder(int gubun);

}
