package kr.or.kimsn.radarsms.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import kr.or.kimsn.radarsms.dto.MenuDto;
import kr.or.kimsn.radarsms.dto.ReceiveConditionDto;
import kr.or.kimsn.radarsms.dto.ReceiveConditionJoinStationRdrDto;
import kr.or.kimsn.radarsms.dto.StationDto;
import kr.or.kimsn.radarsms.repository.MenuRepository;
import kr.or.kimsn.radarsms.repository.ReceiveConditionRepository;
import kr.or.kimsn.radarsms.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final StationRepository stationRepository;
    private final ReceiveConditionRepository receiveConditionRepository;

    // 메뉴조회
    public List<MenuDto> getMenuList() {
        return menuRepository.findAll(Sort.by("order"));
    }

    // 지점조회
    public List<StationDto> getStationList() {
        return stationRepository.findByOrderBySortOrder();
    }

    // main 최종 수신 확인 시각(NQC)
    public List<ReceiveConditionJoinStationRdrDto> getReceiveConditionJoinStationRdrList(List<StationDto> stationList) {
        List<ReceiveConditionJoinStationRdrDto> rcToRdr = new ArrayList<ReceiveConditionJoinStationRdrDto>();
        // 최총 수신 확인
        List<ReceiveConditionDto> rcList = receiveConditionRepository.findByDataTypeOrderBySite("NQC");

        for (StationDto sr : stationList) {
            for (ReceiveConditionDto rc : rcList) {
                if (sr.getSiteCd().equals(rc.getSite())) {
                    ReceiveConditionJoinStationRdrDto rcDto = new ReceiveConditionJoinStationRdrDto();
                    rcDto.setSite(rc.getSite());
                    rcDto.setDataKind(rc.getData_kind());
                    rcDto.setDataType(rc.getDataType());
                    rcDto.setRecv_condition(rc.getRecv_condition());
                    rcDto.setApply_time(rc.getApply_time());
                    rcDto.setLast_check_time(rc.getLast_check_time());
                    rcDto.setSms_send(rc.getSms_send());
                    rcDto.setSms_send_activation(rc.getSms_send_activation());
                    rcDto.setStatus(rc.getStatus());

                    rcDto.setName_kr(sr.getName_kr());
                    rcDto.setGubun(sr.getGubun());
                    rcDto.setPermitted_watch(sr.getPermitted_watch());

                    rcToRdr.add(rcDto);
                }
            }
        }
        return rcToRdr;
    }

    // 메뉴저장
    @Transactional
    public MenuDto addMenuDto(MenuDto menuDto) throws Exception {
        try {
            // menuDto = menuRepository.save(menuDto);
        } catch (Exception e) {
            log.info("insert error : " + e);
        }

        return menuDto;
    }
}
