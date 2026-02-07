package com.comp5348.emailservice.repository;

import com.comp5348.emailservice.model.Email;
import com.comp5348.emailservice.model.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    List<Email> findByStatus(EmailStatus status);
}
