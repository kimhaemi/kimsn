package kr.or.kimsn.radarsms.service;

import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import kr.or.kimsn.radarsms.dto.MembersDto;
import kr.or.kimsn.radarsms.repository.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MembersService {

    private final MembersRepository membersRepository;

    // 사용자 조회
    public MembersDto getUserId(String userId) {
        return membersRepository.findByMemberId(userId);
    }

    // 사용자 리스트 조회
    public List<MembersDto> getUsersList() {
        return membersRepository.findAll();
    }

    // 사용자 단건 조회
    public MembersDto getUsersData(Long id) {
        return membersRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Member Not Found"));
    }

    // 사용자 등록
    @Transactional
    public MembersDto userAdd(MembersDto membersDto) {
        try {
            log.info("사용자 등록 userAdd");
            membersDto = membersRepository.save(membersDto);
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return membersDto;
    }

    // 사용자 수정
    @Transactional
    public MembersDto userModify(MembersDto membersDto) {
        try {
            log.info("사용자 수정 usermodify");
            membersDto = membersRepository.save(membersDto);
        } catch (Exception e) {
            log.info("insert error : " + e);
        }
        return membersDto;
    }

    // 사용자 삭제
    @Transactional
    public Long userDelete(Long userId) {
        try {
            log.info("사용자 삭제 userDelete");
            membersRepository.deleteById(userId);
        } catch (Exception e) {
            log.info("delete error : " + e);
        }
        return userId;
    }
}
