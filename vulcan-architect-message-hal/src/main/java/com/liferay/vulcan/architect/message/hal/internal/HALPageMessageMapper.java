/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.vulcan.architect.message.hal.internal;

import com.liferay.vulcan.architect.list.FunctionalList;
import com.liferay.vulcan.architect.message.json.JSONObjectBuilder;
import com.liferay.vulcan.architect.message.json.PageMessageMapper;
import com.liferay.vulcan.architect.resource.Representor;
import com.liferay.vulcan.architect.resource.identifier.Identifier;
import com.liferay.vulcan.architect.wiring.osgi.manager.CollectionResourceManager;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Adds Vulcan the ability to represent collection pages in HAL format.
 *
 * @author Alejandro Hernández
 * @author Carlos Sierra Andrés
 * @author Jorge Ferrer
 * @see    <a href="http://stateless.co/hal_specification.html">HAL</a>
 * @review
 */
@Component(immediate = true)
public class HALPageMessageMapper<T> implements PageMessageMapper<T> {

	@Override
	public String getMediaType() {
		return "application/hal+json";
	}

	@Override
	public void mapCollectionURL(
		JSONObjectBuilder jsonObjectBuilder, String url) {

		jsonObjectBuilder.nestedField(
			"_links", "collection", "href"
		).value(
			url
		);
	}

	@Override
	public void mapCurrentPageURL(
		JSONObjectBuilder jsonObjectBuilder, String url) {

		jsonObjectBuilder.nestedField(
			"_links", "self", "href"
		).value(
			url
		);
	}

	@Override
	public void mapFirstPageURL(
		JSONObjectBuilder jsonObjectBuilder, String url) {

		jsonObjectBuilder.nestedField(
			"_links", "first", "href"
		).value(
			url
		);
	}

	@Override
	public void mapItemEmbeddedResourceField(
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder,
		FunctionalList<String> embeddedPathElements, String fieldName,
		Object value) {

		_halSingleModelMessageMapper.mapEmbeddedResourceField(
			itemJSONObjectBuilder, embeddedPathElements, fieldName, value);
	}

	@Override
	public void mapItemEmbeddedResourceLink(
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder,
		FunctionalList<String> embeddedPathElements, String fieldName,
		String url) {

		_halSingleModelMessageMapper.mapEmbeddedResourceLink(
			itemJSONObjectBuilder, embeddedPathElements, fieldName, url);
	}

	@Override
	public void mapItemEmbeddedResourceURL(
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder,
		FunctionalList<String> embeddedPathElements, String url) {

		_halSingleModelMessageMapper.mapEmbeddedResourceURL(
			itemJSONObjectBuilder, embeddedPathElements, url);
	}

	@Override
	public void mapItemField(
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder, String fieldName,
		Object value) {

		itemJSONObjectBuilder.field(
			fieldName
		).value(
			value
		);
	}

	@Override
	public void mapItemLink(
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder, String fieldName, String url) {

		_halSingleModelMessageMapper.mapLink(
			itemJSONObjectBuilder, fieldName, url);
	}

	@Override
	public void mapItemLinkedResourceURL(
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder,
		FunctionalList<String> embeddedPathElements, String url) {

		_halSingleModelMessageMapper.mapLinkedResourceURL(
			itemJSONObjectBuilder, embeddedPathElements, url);
	}

	@Override
	public void mapItemSelfURL(
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder, String url) {

		_halSingleModelMessageMapper.mapSelfURL(itemJSONObjectBuilder, url);
	}

	@Override
	public void mapItemTotalCount(
		JSONObjectBuilder jsonObjectBuilder, int totalCount) {

		jsonObjectBuilder.field(
			"total"
		).value(
			totalCount
		);
	}

	@Override
	public void mapItemTypes(
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder, List<String> types) {

		_halSingleModelMessageMapper.mapTypes(itemJSONObjectBuilder, types);
	}

	@Override
	public void mapLastPageURL(
		JSONObjectBuilder jsonObjectBuilder, String url) {

		jsonObjectBuilder.nestedField(
			"_links", "last", "href"
		).value(
			url
		);
	}

	@Override
	public void mapNextPageURL(
		JSONObjectBuilder jsonObjectBuilder, String url) {

		jsonObjectBuilder.nestedField(
			"_links", "next", "href"
		).value(
			url
		);
	}

	@Override
	public void mapPageCount(JSONObjectBuilder jsonObjectBuilder, int count) {
		jsonObjectBuilder.field(
			"count"
		).value(
			count
		);
	}

	@Override
	public void mapPreviousPageURL(
		JSONObjectBuilder jsonObjectBuilder, String url) {

		jsonObjectBuilder.nestedField(
			"_links", "prev", "href"
		).value(
			url
		);
	}

	@Override
	public void onFinishItem(
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder, T model, Class<T> modelClass,
		HttpHeaders httpHeaders) {

		Optional<Representor<T, Identifier>> optional =
			_collectionResourceManager.getRepresentorOptional(modelClass);

		optional.map(
			Representor::getTypes
		).ifPresent(
			types -> pageJSONObjectBuilder.nestedField(
				"_embedded", types.get(0)
			).arrayValue(
			).add(
				itemJSONObjectBuilder
			)
		);
	}

	@Reference
	private CollectionResourceManager _collectionResourceManager;

	@Reference
	private HALSingleModelMessageMapper _halSingleModelMessageMapper;

}