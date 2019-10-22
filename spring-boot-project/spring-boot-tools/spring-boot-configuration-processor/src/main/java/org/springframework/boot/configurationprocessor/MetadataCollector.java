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

package org.springframework.boot.configurationprocessor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;

/**
 * Used by {@link ConfigurationMetadataAnnotationProcessor} to collect
 * {@link ConfigurationMetadata}.
 *
 * @author Andy Wilkinson
 * @author Kris De Volder
 * @since 1.2.2
 */
public class MetadataCollector {

	private final Set<ItemMetadata> metadataItems = new LinkedHashSet<>();

	private final ProcessingEnvironment processingEnvironment;

	private final ConfigurationMetadata previousMetadata;

	private final TypeUtils typeUtils;

	private final Set<String> processedSourceTypes = new HashSet<>();

	/**
	 * Creates a new {@code MetadataProcessor} instance.
	 * @param processingEnvironment the processing environment of the build
	 * @param previousMetadata any previous metadata or {@code null}
	 */
	public MetadataCollector(ProcessingEnvironment processingEnvironment, ConfigurationMetadata previousMetadata) {
		this.processingEnvironment = processingEnvironment;
		this.previousMetadata = previousMetadata;
		this.typeUtils = new TypeUtils(processingEnvironment);
	}

	public void processing(RoundEnvironment roundEnv) {
		roundEnv.getRootElements().forEach(this::markAsProcessed);
	}

	private void markAsProcessed(Element element) {
		if (element instanceof TypeElement) {
			this.processedSourceTypes.add(this.typeUtils.getQualifiedName(element));
		}
	}

	public void add(ItemMetadata metadata) {
		this.metadataItems.add(metadata);
	}

	public boolean hasSimilarGroup(ItemMetadata metadata) {
		if (!metadata.isOfItemType(ItemMetadata.ItemType.GROUP)) {
			throw new IllegalStateException(new StringBuilder().append("item ").append(metadata).append(" must be a group").toString());
		}
		return this.metadataItems.stream().anyMatch(existing -> existing.isOfItemType(ItemMetadata.ItemType.GROUP) && existing.getName().equals(metadata.getName())
				&& existing.getType().equals(metadata.getType()));
	}

	public ConfigurationMetadata getMetadata() {
		ConfigurationMetadata metadata = new ConfigurationMetadata();
		this.metadataItems.forEach(metadata::add);
		if (this.previousMetadata != null) {
			List<ItemMetadata> items = this.previousMetadata.getItems();
			items.stream().filter(this::shouldBeMerged).forEach(metadata::add);
		}
		return metadata;
	}

	private boolean shouldBeMerged(ItemMetadata itemMetadata) {
		String sourceType = itemMetadata.getSourceType();
		return (sourceType != null && !deletedInCurrentBuild(sourceType) && !processedInCurrentBuild(sourceType));
	}

	private boolean deletedInCurrentBuild(String sourceType) {
		return this.processingEnvironment.getElementUtils().getTypeElement(sourceType) == null;
	}

	private boolean processedInCurrentBuild(String sourceType) {
		return this.processedSourceTypes.contains(sourceType);
	}

}
