package com.cmsz.jy.oc.dcc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OcDccProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcDccProxyApplication.class, args);
	}
}
