package com.example.demo.repository;

import com.example.demo.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity,Long> {
    @Query("select m from MemberEntity m ")
    Page<MemberEntity> pagingList(PageRequest pageRequest);
}
