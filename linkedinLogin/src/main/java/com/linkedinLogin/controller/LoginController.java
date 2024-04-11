package com.linkedinLogin.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.linkedinLogin.service.LoginService;

@RestController
public class LoginController {
	
		@Autowired
	    private LoginService loginService;

	    @GetMapping("/login/linkedin")
	    public RedirectView login() {
	        String authorizationUrl = loginService.generateAuthorizationUrl();
	        return new RedirectView(authorizationUrl);
	    }

	    @GetMapping("/callback/linkedin")
	    public String callbackEndpoint(@RequestParam("code") String code) {
	        return loginService.getUserInfoWithToken(code);
	    }
}
