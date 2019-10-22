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

package org.springframework.boot.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * An {@link ApplicationListener} that halts application startup if the system file
 * encoding does not match an expected value set in the environment. By default has no
 * effect, but if you set {@code spring.mandatory_file_encoding} (or some camelCase or
 * UPPERCASE variant of that) to the name of a character encoding (e.g. "UTF-8") then this
 * initializer throws an exception when the {@code file.encoding} System property does not
 * equal it.
 *
 * <p>
 * The System property {@code file.encoding} is normally set by the JVM in response to the
 * {@code LANG} or {@code LC_ALL} environment variables. It is used (along with other
 * platform-dependent variables keyed off those environment variables) to encode JVM
 * arguments as well as file names and paths. In most cases you can override the file
 * encoding System property on the command line (with standard JVM features), but also
 * consider setting the {@code LANG} environment variable to an explicit
 * character-encoding value (e.g. "en_GB.UTF-8").
 *
 * @author Dave Syer
 * @author Madhura Bhave
 * @since 1.0.0
 */
public class FileEncodingApplicationListener
		implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

	private static final Log logger = LogFactory.getLog(FileEncodingApplicationListener.class);

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment environment = event.getEnvironment();
		if (!environment.containsProperty("spring.mandatory-file-encoding")) {
			return;
		}
		String encoding = System.getProperty("file.encoding");
		String desired = environment.getProperty("spring.mandatory-file-encoding");
		if (!(encoding != null && !desired.equalsIgnoreCase(encoding))) {
			return;
		}
		logger.error(new StringBuilder().append("System property 'file.encoding' is currently '").append(encoding).append("'. It should be '").append(desired).append("' (as defined in 'spring.mandatoryFileEncoding').").toString());
		logger.error(new StringBuilder().append("Environment variable LANG is '").append(System.getenv("LANG")).append("'. You could use a locale setting that matches encoding='").append(desired).append("'.").toString());
		logger.error(new StringBuilder().append("Environment variable LC_ALL is '").append(System.getenv("LC_ALL")).append("'. You could use a locale setting that matches encoding='").append(desired).append("'.").toString());
		throw new IllegalStateException(new StringBuilder().append("The Java Virtual Machine has not been configured to use the ").append("desired default character encoding (").append(desired).append(").").toString());
	}

}
