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

package smoketest.jndi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class WebController {

	private static final Logger logger = LoggerFactory.getLogger(WebController.class);

	private final AccountService service;

	private final AccountRepository repository;

	public WebController(AccountService service, AccountRepository repository) {
		this.service = service;
		this.repository = repository;
	}

	@GetMapping("/")
	public String hello() {
		logger.info("Count is " + this.repository.count());
		this.service.createAccountAndNotify("josh");
		try {
			this.service.createAccountAndNotify("error");
		}
		catch (Exception ex) {
			logger.info(ex.getMessage(), ex);
		}
		long count = this.repository.count();
		logger.info("Count is " + count);
		return "Count is " + count;
	}

}
