package kr.or.kimsn.radardemo.dto.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radardemo.dto.StationDto;

public interface StationRepository extends JpaRepository<StationDto, String> {

    List<StationDto> findByOrderBySortOrder();

    List<StationDto> findByGubunOrderBySortOrder(int gubun);

    StationDto findBySiteCdOrderBySortOrder(String siteCd);

}
