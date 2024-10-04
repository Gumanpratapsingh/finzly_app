package com.guman.bbc_backend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BbcBackendApplication {

//	@Bean(name = "processBatchExecutor")
//	public ThreadPoolTaskExecutor processBatchExecutor() throws  InterruptedException{
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(500);
//		executor.setMaxPoolSize(500);
//		executor.setQueueCapacity(10000);
//		executor.setThreadNamePrefix("CSV thread - ");
//		executor.initialize();
//		return executor;
//	}

	public static void main(String[] args) {

		SpringApplication.run(BbcBackendApplication.class, args);
	}
}