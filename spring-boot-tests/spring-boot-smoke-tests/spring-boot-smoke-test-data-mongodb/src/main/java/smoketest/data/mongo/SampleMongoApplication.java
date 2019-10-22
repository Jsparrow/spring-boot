/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package smoketest.data.mongo;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class SampleMongoApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(SampleMongoApplication.class);
	@Autowired
	private CustomerRepository repository;

	@Override
	public void run(String... args) throws Exception {
		this.repository.deleteAll();

		// save a couple of customers
		this.repository.save(new Customer("Alice", "Smith"));
		this.repository.save(new Customer("Bob", "Smith"));

		// fetch all customers
		logger.info("Customers found with findAll():");
		logger.info("-------------------------------");
		this.repository.findAll().forEach(customer -> logger.info(String.valueOf(customer)));
		System.out.println();

		// fetch an individual customer
		logger.info("Customer found with findByFirstName('Alice'):");
		logger.info("--------------------------------");
		logger.info(String.valueOf(this.repository.findByFirstName("Alice")));

		logger.info("Customers found with findByLastName('Smith'):");
		logger.info("--------------------------------");
		this.repository.findByLastName("Smith").forEach(customer -> logger.info(String.valueOf(customer)));
	}

	@Bean
	public MongoClientSettingsBuilderCustomizer customizer() {
		return (builder) -> builder.applyToConnectionPoolSettings(
				(connectionPool) -> connectionPool.maxConnectionIdleTime(5, TimeUnit.MINUTES));
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleMongoApplication.class, args);
	}

}
