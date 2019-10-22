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

package org.springframework.boot.actuate.autoconfigure.web.servlet;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextFactory;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ManagementContextFactory} for servlet-based web applications.
 *
 * @author Andy Wilkinson
 */
class ServletManagementContextFactory implements ManagementContextFactory {

	private static final Logger logger = LoggerFactory.getLogger(ServletManagementContextFactory.class);

	@Override
	public ConfigurableWebServerApplicationContext createManagementContext(ApplicationContext parent,
			Class<?>... configClasses) {
		AnnotationConfigServletWebServerApplicationContext child = new AnnotationConfigServletWebServerApplicationContext();
		child.setParent(parent);
		List<Class<?>> combinedClasses = new ArrayList<>(Arrays.asList(configClasses));
		combinedClasses.add(ServletWebServerFactoryAutoConfiguration.class);
		child.register(ClassUtils.toClassArray(combinedClasses));
		registerServletWebServerFactory(parent, child);
		return child;
	}

	private void registerServletWebServerFactory(ApplicationContext parent,
			AnnotationConfigServletWebServerApplicationContext childContext) {
		try {
			ConfigurableListableBeanFactory beanFactory = childContext.getBeanFactory();
			if (beanFactory instanceof BeanDefinitionRegistry) {
				BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
				registry.registerBeanDefinition("ServletWebServerFactory",
						new RootBeanDefinition(determineServletWebServerFactoryClass(parent)));
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			logger.error(ex.getMessage(), ex);
			// Ignore and assume auto-configuration
		}
	}

	private Class<?> determineServletWebServerFactoryClass(ApplicationContext parent) {
		Class<?> factoryClass = parent.getBean(ServletWebServerFactory.class).getClass();
		if (cannotBeInstantiated(factoryClass)) {
			throw new FatalBeanException(new StringBuilder().append("ServletWebServerFactory implementation ").append(factoryClass.getName()).append(" cannot be instantiated. To allow a separate management port to be used, a top-level class ").append("or static inner class should be used instead").toString());
		}
		return factoryClass;
	}

	private boolean cannotBeInstantiated(Class<?> factoryClass) {
		return factoryClass.isLocalClass()
				|| (factoryClass.isMemberClass() && !Modifier.isStatic(factoryClass.getModifiers()))
				|| factoryClass.isAnonymousClass();
	}

}
