package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberFileDTO {
    private Long id;
    private String originalFileName;
    private String serverFileName;
}
