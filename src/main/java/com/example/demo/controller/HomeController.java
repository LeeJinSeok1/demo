package com.example.demo.controller;

import com.example.demo.config.PathConfig;
import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.MemberFileDTO;
import com.example.demo.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Tag(name = "Member API", description = "회원 관련 API")
public class HomeController {
    private final PathConfig pathConfig;
    private final MemberService memberService;

    @GetMapping("/")
    public String home(Model model) {
        List<MemberDTO> memberDTOList = memberService.findAll();
        model.addAttribute("memberList",memberDTOList);
        return "index";
    }

    @GetMapping("/savePage")
    public String savePage(@RequestParam(required = false) Long id, Model model){
        MemberDTO memberDTO;
        List<MemberFileDTO> nullList = new ArrayList<>();
        if(id == null){
            // 빈 객체 생성
            memberDTO = new MemberDTO();
            memberDTO.setMemberFileDTOList(nullList); // null이면 뷰에서 오류 발생할 수 있으므로
        } else {
            memberDTO = memberService.findById(id);
            if(memberDTO.getMemberFileDTOList() == null || memberDTO.getMemberFileDTOList().isEmpty()){
                memberDTO.setMemberFileDTOList(nullList); // 안전하게 초기화
            }
        }
        model.addAttribute("member", memberDTO);
        return "savePage";
    }

    @ResponseBody
    @PostMapping("/save")
    @Operation(summary = "회원 조회", description = "ID로 회원 정보 조회")
    public String save(MemberDTO memberDTO){
        return memberService.save(memberDTO);
    }
    @GetMapping("/detail")
    public String detail(Model model, @RequestParam("id") Long id){
        if(id == null){
            return "redirect:/";
        }
        MemberDTO memberDTO = memberService.findById(id);
        if(memberDTO == null){
            return "redirect:/";
        }
        model.addAttribute("path",pathConfig.memberFilePath);
        model.addAttribute("member",memberDTO);
        return "detail";
    }

    @ResponseBody
    @DeleteMapping("/delete")
    public String delete(@RequestParam("id") Long id){
        return memberService.delete(id);
    }

    @GetMapping("/pagingList")
    public String pagingList(@PageableDefault(page = 1) Pageable pageable,
                             Model model){
        final int pageLimit = 5;

        Page<MemberDTO> memberPagingList = memberService.pagingList(pageable,pageLimit);

        int blockLimit = 10;
        int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
        int endPage = Math.min((startPage + blockLimit - 1), memberPagingList.getTotalPages());
        model.addAttribute("memberList", memberPagingList);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("nowPage",pageable.getPageNumber());
        model.addAttribute("totalPages",memberPagingList.getTotalPages());
        model.addAttribute("total",memberPagingList.getTotalElements());
        return "pagingList";
    }

}
