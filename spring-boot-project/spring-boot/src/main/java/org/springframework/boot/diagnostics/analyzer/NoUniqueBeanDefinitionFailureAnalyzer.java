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

package org.springframework.boot.diagnostics.analyzer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AbstractInjectionFailureAnalyzer} that performs analysis of failures caused
 * by a {@link NoUniqueBeanDefinitionException}.
 *
 * @author Andy Wilkinson
 */
class NoUniqueBeanDefinitionFailureAnalyzer extends AbstractInjectionFailureAnalyzer<NoUniqueBeanDefinitionException>
		implements BeanFactoryAware {

	private static final Logger logger = LoggerFactory.getLogger(NoUniqueBeanDefinitionFailureAnalyzer.class);
	private ConfigurableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isInstanceOf(ConfigurableBeanFactory.class, beanFactory);
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, NoUniqueBeanDefinitionException cause,
			String description) {
		if (description == null) {
			return null;
		}
		String[] beanNames = extractBeanNames(cause);
		if (beanNames == null) {
			return null;
		}
		StringBuilder message = new StringBuilder();
		message.append(String.format("%s required a single bean, but %d were found:%n", description, beanNames.length));
		for (String beanName : beanNames) {
			buildMessage(message, beanName);
		}
		return new FailureAnalysis(message.toString(),
				new StringBuilder().append("Consider marking one of the beans as @Primary, updating the consumer to").append(" accept multiple beans, or using @Qualifier to identify the").append(" bean that should be consumed").toString(),
				cause);
	}

	private void buildMessage(StringBuilder message, String beanName) {
		try {
			BeanDefinition definition = this.beanFactory.getMergedBeanDefinition(beanName);
			message.append(getDefinitionDescription(beanName, definition));
		}
		catch (NoSuchBeanDefinitionException ex) {
			logger.error(ex.getMessage(), ex);
			message.append(String.format("\t- %s: a programmatically registered singleton", beanName));
		}
	}

	private String getDefinitionDescription(String beanName, BeanDefinition definition) {
		if (StringUtils.hasText(definition.getFactoryMethodName())) {
			return String.format("\t- %s: defined by method '%s' in %s%n", beanName, definition.getFactoryMethodName(),
					definition.getResourceDescription());
		}
		return String.format("\t- %s: defined in %s%n", beanName, definition.getResourceDescription());
	}

	private String[] extractBeanNames(NoUniqueBeanDefinitionException cause) {
		if (cause.getMessage().contains("but found")) {
			return StringUtils.commaDelimitedListToStringArray(
					cause.getMessage().substring(cause.getMessage().lastIndexOf(':') + 1).trim());
		}
		return null;
	}

}
