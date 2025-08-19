package com.example.demo.controller;

import com.example.demo.config.PathConfig;
import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.MemberFileDTO;
import com.example.demo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
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
    public String savePage(@RequestParam(required = false) Long id,Model model){
        if(id == null){
            model.addAttribute("member",null);
        }else{
            MemberDTO memberDTO1 = memberService.findById(id);
            model.addAttribute("member",memberDTO1);
        }
        return "savePage";
    }
    @ResponseBody
    @PostMapping("/save")
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
