package com.pnu.springsecuritytest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class SpringSecurityTestApplicationTests {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        String testPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(testPassword);
        System.out.println("Encoded Password: " + encodedPassword);
    }

}
