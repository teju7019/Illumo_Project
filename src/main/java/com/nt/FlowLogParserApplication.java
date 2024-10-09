package com.nt;

import com.nt.service.FlowLogParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlowLogParserApplication implements CommandLineRunner {

	@Autowired
	private FlowLogParserService flowLogParserService;

	public static void main(String[] args) {
		SpringApplication.run(FlowLogParserApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Start parsing flow logs
		flowLogParserService.processLogs();
	}
}
