package com.example.security;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

public class EncryptionTests {

	@Test
	public void testBCrypt() {
		// BCrypt.gensalt() has 10 log_rounds as default (reasonable, tolerable, performance-wise)
		System.out.println(BCrypt.hashpw("hello", BCrypt.gensalt()));
		assertTrue(BCrypt.checkpw("hello", "$2a$12$qevbPfTUlQuWEONbtCGB5elNcsV3LvQdihhGAXUfwORAdKYZ6zkGK"));
	}

	@Test
	public void testBCryptPasswordEncoder() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(encoder.encode("hello world"));
		assertTrue(encoder.matches("hello world", "$2a$12$zv35Nvg5UNK9FT.jMjtIGu/BEaA1ZCNgJAZVEJDcE29/ppgAtoaa."));
	}

	@Test
	public void testKeyGenerator() {
		StringKeyGenerator generator = KeyGenerators.string();
		System.out.println(generator.generateKey());
	}

	@Test
	public void testEncryptor() {
		TextEncryptor encryptor = Encryptors.delux("password", "f25e5ba1a7d42dc1");
		String plainText = "hello world";
		String encryptedText = encryptor.encrypt(plainText);
		System.out.println(encryptedText);
		assertThat(plainText, is(encryptor.decrypt(encryptedText)));
	}

}
