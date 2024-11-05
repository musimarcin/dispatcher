package com.api.dispatcher;

import org.springframework.boot.SpringApplication;

public class TestDispatcherApplication {

	public static void main(String[] args) {
		SpringApplication.from(DispatcherApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
