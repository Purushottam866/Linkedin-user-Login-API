package com.linkedinLogin.dto;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Component
@Data
@Entity
public class LoginDto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String linkedinprofileid;
	private String linkedinprofilename;
	private String linkedinprofileemail;
	@Column(length = 1000)
	private String linkedinprofileaccesstoken;
}
