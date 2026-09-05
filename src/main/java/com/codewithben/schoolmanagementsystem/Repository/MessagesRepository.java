package com.codewithben.schoolmanagementsystem.Repository;

import com.codewithben.schoolmanagementsystem.Entity.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessagesRepository extends JpaRepository<Messages, Long> {
}
