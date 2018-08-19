package com.example.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MainApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void testBCrypt() {
		// BCrypt.gensalt() has 10 log_rounds as default (reasonable, tolerable, performance-wise)
		System.out.println(BCrypt.hashpw("hello", BCrypt.gensalt()));
		System.out.println(BCrypt.checkpw("hello", "$2a$12$qevbPfTUlQuWEONbtCGB5elNcsV3LvQdihhGAXUfwORAdKYZ6zkGK"));
	}

	@Test
	public void testBCryptPasswordEncoder() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(encoder.encode("hello world"));
		System.out.println(encoder.matches("hello world", "$2a$12$zv35Nvg5UNK9FT.jMjtIGu/BEaA1ZCNgJAZVEJDcE29/ppgAtoaa."));
	}

}
