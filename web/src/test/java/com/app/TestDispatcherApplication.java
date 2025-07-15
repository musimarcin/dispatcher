package com.app;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestDispatcherApplication {

	public static void main(String[] args) {
		SpringApplication.from(DispatcherApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
