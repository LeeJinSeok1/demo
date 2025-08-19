package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "memberFile")
public class MemberFileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String originalFileName;
    private String serverFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="memberEntity")
    private MemberEntity memberEntity;
}
