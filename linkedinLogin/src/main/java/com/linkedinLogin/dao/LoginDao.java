package com.linkedinLogin.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.linkedinLogin.dto.LoginDto;
import com.linkedinLogin.repository.LoginRepository;

@Service
public class LoginDao {
	
	@Autowired
	LoginRepository loginRepository;
	
	public void save(LoginDto loginDto)
	{
		loginRepository.save(loginDto);
	}
}
