package com.restomgmt.site;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"com.restomgmt.site.user.repositories", "com.restomgmt.site.menu.repositories"})
//@EnableMongoRepositories(basePackages = "com.restomgmt.site.mongo.repositories")//placeholder
public class SiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiteApplication.class, args);
	}

}
