package com.linkedinLogin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.linkedinLogin.dto.LoginDto;

@Repository
public interface LoginRepository extends JpaRepository<LoginDto, Integer>{
	
	
}
