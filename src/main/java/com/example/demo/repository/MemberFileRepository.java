package com.example.demo.repository;

import com.example.demo.dto.MemberFileDTO;
import com.example.demo.entity.MemberEntity;
import com.example.demo.entity.MemberFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberFileRepository extends JpaRepository<MemberFileEntity,Long> {
    List<MemberFileEntity> findAllByMemberEntity(MemberEntity memberEntity);
}
