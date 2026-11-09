package com.cal.archivum.entity;


import com.cal.archivum.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "character_relationships")
public class CharacterRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private Long id;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "from_character_id", nullable = false)
    private WorldCharacter fromCharacter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_character_id",nullable = false)
    private WorldCharacter toCharacter;

    @Enumerated(EnumType.STRING)
    @Column(name ="relationship_type", nullable = false)
    private RelationshipType relationshipType;
}