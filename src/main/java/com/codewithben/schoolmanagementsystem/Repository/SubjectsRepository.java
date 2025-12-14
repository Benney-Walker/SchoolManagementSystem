package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Subjects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectsRepository extends JpaRepository<Subjects, Long> {
    Optional<Subjects> findBySubjectName(String subjectName);

    Optional<Subjects> findBySubjectId(String subjectId);
}
