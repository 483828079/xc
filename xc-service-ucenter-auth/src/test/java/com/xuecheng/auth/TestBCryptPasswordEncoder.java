package com.xuecheng.auth;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestBCryptPasswordEncoder {
	@Test
	public void testPasswrodEncoder(){
		String password = "zzz";
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		// 按照随机盐值加密
		String encode = passwordEncoder.encode(password);
		System.out.println(encode);
		boolean isEqual = passwordEncoder.matches(password, encode);
		System.out.println(isEqual);
	}
}
