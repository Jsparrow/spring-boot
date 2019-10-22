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

package org.springframework.boot.configurationdocs;

import org.junit.jupiter.api.Test;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationTable}.
 *
 * @author Brian Clozel
 */
class ConfigurationTableTests {

	private static String newline = System.lineSeparator();

	@Test
	void simpleTable() {
		ConfigurationTable table = new ConfigurationTable("test");
		ConfigurationMetadataProperty first = new ConfigurationMetadataProperty();
		first.setId("spring.test.prop");
		first.setDefaultValue("something");
		first.setDescription("This is a description.");
		first.setType("java.lang.String");
		ConfigurationMetadataProperty second = new ConfigurationMetadataProperty();
		second.setId("spring.test.other");
		second.setDefaultValue("other value");
		second.setDescription("This is another description.");
		second.setType("java.lang.String");
		table.addEntry(new SingleConfigurationTableEntry(first));
		table.addEntry(new SingleConfigurationTableEntry(second));
		assertThat(table.toAsciidocTable()).isEqualTo(new StringBuilder().append("[cols=\"1,1,2\", options=\"header\"]").append(newline).append("|===").append(newline).append("|Key|Default Value|Description").append(newline)
				.append(newline).append("|`+spring.test.other+`").append(newline).append("|`+other value+`").append(newline).append("|+++This is another description.+++").append(newline)
				.append(newline).append("|`+spring.test.prop+`").append(newline).append("|`+something+`").append(newline).append("|+++This is a description.+++").append(newline)
				.append(newline).append("|===").append(newline).toString());
	}

}
