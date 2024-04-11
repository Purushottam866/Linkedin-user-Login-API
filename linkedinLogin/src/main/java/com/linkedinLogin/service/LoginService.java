package com.linkedinLogin.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedinLogin.dao.LoginDao;
import com.linkedinLogin.dto.LoginDto;

@Service
public class LoginService {
	
	 	@Value("${linkedin.clientId}")
	    private String clientId;

	    @Value("${linkedin.clientSecret}")
	    private String clientSecret;

	    @Value("${linkedin.redirectUri}")
	    private String redirectUri;

	    @Value("${linkedin.scope}")
	    private String scope;

	    @Autowired
	    private RestTemplate restTemplate;

	    @Autowired
	    private HttpHeaders httpHeaders;
	    
	    @Autowired
	    LoginDto loginDto;
	    
	    @Autowired
	    LoginDao loginDao;

	    public String generateAuthorizationUrl() {
	        return "https://www.linkedin.com/oauth/v2/authorization" +
	                "?response_type=code" +
	                "&client_id=" + clientId +
	                "&redirect_uri=" + redirectUri +
	                "&scope=" + scope;
	    }

	    public String exchangeAuthorizationCodeForAccessToken(String code) throws IOException {
	        String accessTokenUrl = "https://www.linkedin.com/oauth/v2/accessToken";
	        String params = "grant_type=authorization_code" +
	                "&code=" + code +
	                "&redirect_uri=" + redirectUri +
	                "&client_id=" + clientId +
	                "&client_secret=" + clientSecret;

	        URL url = new URL(accessTokenUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        connection.setDoOutput(true);

	          try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
	            byte[] input = params.getBytes(StandardCharsets.UTF_8);
	            wr.write(input, 0, input.length);
	        }

	        int responseCode = connection.getResponseCode();

	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            String response = parseResponse(connection);
	            return parseAccessToken(response);
	        } else {
	            return null;
	        }
	    }

	    public String getUserInfo(String accessToken) {
	        String userInfoUrl = "https://api.linkedin.com/v2/userinfo";

	        httpHeaders.setBearerAuth(accessToken);
	        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

	        try {
	            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
	            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);
	            if (response.getStatusCode() == HttpStatus.OK) {
	                ObjectMapper objectMapper = new ObjectMapper();
	                JsonNode userInfo = objectMapper.readTree(response.getBody());
	                String sub = userInfo.get("sub").asText();
	                String name = userInfo.get("name").asText();
	                String email = userInfo.get("email").asText();
	                return "Sub: " + sub + ", Name: " + name + ", Email: " + email;
	            }
	        } catch (HttpClientErrorException e) {
	            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
	                System.err.println("Unauthorized error: " + e.getMessage());
	            } else {
	                System.err.println("Error: " + e.getMessage());
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    private String parseResponse(HttpURLConnection connection) throws IOException {
	        try (Scanner scanner = new Scanner(connection.getInputStream())) {
	            return scanner.useDelimiter("\\A").next();
	        }
	    }

	    private String parseAccessToken(String response) throws IOException {
	        return new ObjectMapper().readTree(response).get("access_token").asText();
	    }

	    public String getUserInfoWithToken(String code) {
	        try {
	            String accessToken = exchangeAuthorizationCodeForAccessToken(code);
	            if (accessToken != null) {
	            	System.out.println("Accesstoken = "+accessToken);
	                String userInfo = getUserInfo(accessToken);
	                if (userInfo != null) {
	                    // Use regular expression to extract sub, name, and email
	                    Pattern pattern = Pattern.compile("Sub: (.+), Name: (.+), Email: (.+)");
	                    Matcher matcher = pattern.matcher(userInfo);
	                    
	                    if (matcher.find()) {
	                        String sub = matcher.group(1);
	                        String name = matcher.group(2);
	                        String email = matcher.group(3);
	                        
	                        // Now you have sub, name, and email stored in individual variables
	                        System.out.println("Sub: " + sub);
	                        System.out.println("Name: " + name);
	                        System.out.println("Email: " + email);
	                        
	                        loginDto.setLinkedinprofileid(sub);
	                        loginDto.setLinkedinprofilename(name);
	                        loginDto.setLinkedinprofileemail(email);
	                        loginDto.setLinkedinprofileaccesstoken(accessToken);
	                        
	                        loginDao.save(loginDto);
	                        
	                        // You can return or use these variables as needed
	                        return "AccessToken: " + accessToken + "</br>" +
	                                "Sub: " + sub + ", Name: " + name + ", Email: " + email;
	                    } else {
	                        return "Failed to extract user information from the response.";
	                    }
	                } else {
	                    return "Failed to retrieve user information.";
	                }
	            } else {
	                return "Failed to exchange authorization code for access token.";
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	            return "An error occurred while processing the request: " + e.getMessage();
	        }
	    }
}
