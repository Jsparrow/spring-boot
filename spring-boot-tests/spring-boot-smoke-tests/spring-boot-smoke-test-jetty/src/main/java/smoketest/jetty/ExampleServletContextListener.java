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

package smoketest.jetty;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple {@link ServletContextListener} to test gh-2058.
 */
@Component
public class ExampleServletContextListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(ExampleServletContextListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("*** contextInitialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("*** contextDestroyed");
	}

}
