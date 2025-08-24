package com.aiva.user.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "children")
@EntityListeners(AuditingEntityListener::class)
data class Child(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false, unique = true)
    val userId: UUID,
    
    @Column(name = "birth_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val birthType: BirthType,
    
    @Column(name = "birth_date")
    val birthDate: LocalDate? = null,
    
    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    val gender: Gender,
    
    @Column(name = "note", length = 500)
    val note: String? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class BirthType {
    BORN, DUE, DUE_UNKNOWN
}

enum class Gender {
    FEMALE, MALE, UNKNOWN
}
