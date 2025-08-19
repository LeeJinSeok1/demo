package com.example.demo.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@ToString
public class MemberDTO {
    private Long id;
    private String name;
    private String age;
    private String job;
    private String status;
    private List<MultipartFile> fileList;
    private List<MemberFileDTO> memberFileDTOList;
    private List<Long> deleteFileIds;

}
