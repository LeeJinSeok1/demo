package com.example.demo.service;

import com.example.demo.config.PathConfig;
import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.MemberFileDTO;
import com.example.demo.entity.MemberEntity;
import com.example.demo.entity.MemberFileEntity;
import com.example.demo.repository.MemberFileRepository;
import com.example.demo.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final PathConfig pathConfig;
    private final MemberRepository memberRepository;
    private final MemberFileRepository memberFileRepository;
    private final Logger log = LoggerFactory.getLogger(MemberService.class);

    public String save(MemberDTO memberDTO) {
        try {
            MemberEntity memberEntity;

            // 신규 등록
            if ("save".equals(memberDTO.getStatus())) {
                memberEntity = new MemberEntity();
                memberEntity.setName(memberDTO.getName());
                memberEntity.setAge(memberDTO.getAge());
                memberEntity.setJob(memberDTO.getJob());

                // 우선 멤버 저장 (PK 확보)
                memberEntity = memberRepository.save(memberEntity);

                // 파일 저장
                if (memberDTO.getFileList() != null && !memberDTO.getFileList().isEmpty()) {
                    saveFiles(memberDTO.getFileList(), memberEntity);
                }

            }
            // 수정
            else {
                Optional<MemberEntity> optionalEntity = memberRepository.findById(memberDTO.getId());
                if (optionalEntity.isEmpty()) {
                    return "updateFail";
                }

                memberEntity = optionalEntity.get();
                memberEntity.setName(memberDTO.getName());
                memberEntity.setAge(memberDTO.getAge());
                memberEntity.setJob(memberDTO.getJob());

                // 파일 추가 업로드 가능 (옵션)
                if (memberDTO.getFileList() != null && !memberDTO.getFileList().isEmpty()) {
                    saveFiles(memberDTO.getFileList(), memberEntity);
                }

                // 수정시 파일 삭제 처리
                if (memberDTO.getDeleteFileIds() != null && !memberDTO.getDeleteFileIds().isEmpty()) {
                    deleteFiles(memberDTO.getDeleteFileIds());
                }

                memberRepository.save(memberEntity);
            }

            return "success";

        } catch (Exception e) {
            log.error("Member 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("회원 저장 실패", e); // 트랜잭션 롤백 유도
        }
    }

    private void saveFiles(List<MultipartFile> fileList, MemberEntity memberEntity) throws IOException {
        for (MultipartFile file : fileList) {
            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null || originalFileName.isBlank()) {
                continue;
            }

            // 확장자 추출
            String fileExtension = "";
            int dotIdx = originalFileName.lastIndexOf(".");
            if (dotIdx != -1) {
                fileExtension = originalFileName.substring(dotIdx);
            }

            // 저장 파일명 생성
            String serverFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + fileExtension;
            String filePath = pathConfig.memberFilePath + serverFileName;

            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent()); // 디렉토리 없으면 생성
            Files.write(path, file.getBytes());

            // 파일 엔티티 저장
            MemberFileEntity memberFileEntity = new MemberFileEntity();
            memberFileEntity.setOriginalFileName(originalFileName);
            memberFileEntity.setServerFileName(serverFileName);
            memberFileEntity.setMemberEntity(memberEntity);

            memberFileRepository.save(memberFileEntity);
        }
    }


    public List<MemberDTO> findAll() {
        List<MemberEntity> memberEntityList = memberRepository.findAll();
        List<MemberDTO> memberDTOList = new ArrayList<>();
        for (MemberEntity memberEntity : memberEntityList) {
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setName(memberEntity.getName());
            memberDTO.setAge(memberEntity.getAge());
            memberDTO.setJob(memberEntity.getJob());
            memberDTO.setId(memberEntity.getId());
            memberDTOList.add(memberDTO);
        }
        return memberDTOList;
    }

    public MemberDTO findById(Long id) {
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(id);
        if(optionalMemberEntity.isPresent()){
            MemberEntity memberEntity = optionalMemberEntity.get();
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setId(memberEntity.getId());
            memberDTO.setName(memberEntity.getName());
            memberDTO.setAge(memberEntity.getAge());
            memberDTO.setJob(memberEntity.getJob());
            memberDTO.setMemberFileDTOList(getMemberFileList(memberEntity));
            return memberDTO;
        }else{
            return null;
        }
    }

    public String delete(Long id) {
        try {
            Optional<MemberEntity> optionalMemberEntity = memberRepository.findById(id);
            if(optionalMemberEntity.isPresent()){
                memberRepository.deleteById(id);
                return "success";
            }
            return "noData";
        }catch (Exception e){
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }

    public Page<MemberDTO> pagingList(Pageable pageable, int pageLimit) {
        Sort sort = Sort.by("id").descending(); // order 값 없으면 ko
        int page = pageable.getPageNumber() - 1;
        PageRequest pageRequest = PageRequest.of(page, pageLimit, sort);

        Page<MemberEntity> memberEntityList = memberRepository.pagingList(pageRequest);

        Page<MemberDTO> memberDTOList = memberEntityList.map(memberEntity -> {
            MemberDTO dto = new MemberDTO();
            dto.setId(memberEntity.getId());
            dto.setName(memberEntity.getName());
            dto.setAge(memberEntity.getAge());
            dto.setJob(memberEntity.getJob());
            return dto;
        });

        return memberDTOList;
    }
    public List<MemberFileDTO> getMemberFileList(MemberEntity memberEntity) {
        List<MemberFileDTO> memberFileDTOList = new ArrayList<>();
        List<MemberFileEntity> memberFileEntityList = memberFileRepository.findAllByMemberEntity(memberEntity);
        if(memberFileEntityList.isEmpty()){
            return null;
        }else{
            for (MemberFileEntity memberFileEntity : memberFileEntityList) {
                MemberFileDTO memberFileDTO = new MemberFileDTO();
                memberFileDTO.setId(memberFileEntity.getId());
                memberFileDTO.setOriginalFileName(memberFileEntity.getOriginalFileName());
                memberFileDTO.setServerFileName(memberFileEntity.getServerFileName());
                memberFileDTOList.add(memberFileDTO);
            }
            return memberFileDTOList;
        }
    }

    @Transactional
    public void deleteFiles(List<Long> deleteFileIds) throws IOException {
        if (deleteFileIds == null || deleteFileIds.isEmpty()) {
            return;
        }

        for (Long id : deleteFileIds) {

            Optional<MemberFileEntity> fileOpt = memberFileRepository.findById(id);
            if (fileOpt.isPresent()) {
                MemberFileEntity fileEntity = fileOpt.get();

                // 실제 파일 삭제
                Path path = Paths.get(pathConfig.memberFilePath + fileEntity.getServerFileName());
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    // 로그 남기고 계속 진행
                    System.err.println("파일 삭제 실패: " + path + ", " + e.getMessage());
                    throw e; // 필요하면 예외 다시 던져서 트랜잭션 롤백
                }

                // DB에서 삭제
                memberFileRepository.delete(fileEntity);
            }
        }
    }

}

