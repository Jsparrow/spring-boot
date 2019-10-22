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

package smoketest.data.ldap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class SampleLdapApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(SampleLdapApplication.class);
	private final PersonRepository repository;

	public SampleLdapApplication(PersonRepository repository) {
		this.repository = repository;
	}

	@Override
	public void run(String... args) throws Exception {

		// fetch all people
		logger.info("People found with findAll():");
		logger.info("-------------------------------");
		for (Person person : this.repository.findAll()) {
			logger.info(String.valueOf(person));
		}
		System.out.println();

		// fetch an individual person
		logger.info("Person found with findByPhone('+46 555-123456'):");
		logger.info("--------------------------------");
		logger.info(String.valueOf(this.repository.findByPhone("+46 555-123456")));
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleLdapApplication.class, args).close();
	}

}
