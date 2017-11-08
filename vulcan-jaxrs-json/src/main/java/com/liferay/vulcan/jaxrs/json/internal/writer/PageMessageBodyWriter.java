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

package com.liferay.vulcan.jaxrs.json.internal.writer;

import static org.osgi.service.component.annotations.ReferenceCardinality.AT_LEAST_ONE;
import static org.osgi.service.component.annotations.ReferencePolicyOption.GREEDY;

import com.google.gson.JsonObject;

import com.liferay.vulcan.alias.BinaryFunction;
import com.liferay.vulcan.error.VulcanDeveloperError;
import com.liferay.vulcan.error.VulcanDeveloperError.UnresolvableURI;
import com.liferay.vulcan.language.Language;
import com.liferay.vulcan.list.FunctionalList;
import com.liferay.vulcan.message.json.JSONObjectBuilder;
import com.liferay.vulcan.message.json.PageMessageMapper;
import com.liferay.vulcan.pagination.Page;
import com.liferay.vulcan.pagination.SingleModel;
import com.liferay.vulcan.resource.RelatedCollection;
import com.liferay.vulcan.resource.RelatedModel;
import com.liferay.vulcan.resource.Representor;
import com.liferay.vulcan.resource.identifier.Identifier;
import com.liferay.vulcan.response.control.Embedded;
import com.liferay.vulcan.response.control.Fields;
import com.liferay.vulcan.result.Try;
import com.liferay.vulcan.url.ServerURL;
import com.liferay.vulcan.wiring.osgi.manager.CollectionResourceManager;
import com.liferay.vulcan.wiring.osgi.manager.ProviderManager;
import com.liferay.vulcan.wiring.osgi.util.GenericUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.nio.charset.StandardCharsets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Writes collection pages by using the {@link PageMessageMapper} that
 * corresponds to the media type.
 *
 * @author Alejandro Hernández
 * @author Carlos Sierra Andrés
 * @author Jorge Ferrer
 */
@Component(
	immediate = true, property = "liferay.vulcan.message.body.writer=true"
)
@Provider
public class PageMessageBodyWriter<T>
	implements MessageBodyWriter<Try.Success<Page<T>>> {

	@Override
	public long getSize(
		Try.Success<Page<T>> success, Class<?> clazz, Type genericType,
		Annotation[] annotations, MediaType mediaType) {

		return -1;
	}

	@Override
	public boolean isWriteable(
		Class<?> clazz, Type genericType, Annotation[] annotations,
		MediaType mediaType) {

		Try<Class<Object>> classTry =
			GenericUtil.getFirstGenericTypeArgumentTry(genericType);

		return classTry.filter(
			Page.class::equals
		).isSuccess();
	}

	@Override
	public void writeTo(
			Try.Success<Page<T>> success, Class<?> clazz, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream)
		throws IOException, WebApplicationException {

		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
			entityStream, StandardCharsets.UTF_8);

		PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);

		Stream<PageMessageMapper<T>> stream = _pageMessageMappers.stream();

		String mediaTypeString = mediaType.toString();

		Page<T> page = success.getValue();

		PageMessageMapper<T> pageMessageMapper = stream.filter(
			bodyWriter ->
				mediaTypeString.equals(bodyWriter.getMediaType()) &&
				bodyWriter.supports(page, _httpHeaders)
		).findFirst(
		).orElseThrow(
			() -> new VulcanDeveloperError.MustHaveMessageMapper(
				mediaTypeString, page.getModelClass())
		);

		JSONObjectBuilder jsonObjectBuilder = new JSONObjectBuilder();

		pageMessageMapper.onStart(jsonObjectBuilder, page, _httpHeaders);

		Optional<Fields> fieldsOptional = _providerManager.provideOptional(
			Fields.class, _httpServletRequest);

		Fields fields = fieldsOptional.orElseThrow(
			() -> new VulcanDeveloperError.MustHaveProvider(Fields.class));

		Optional<Embedded> embeddedOptional = _providerManager.provideOptional(
			Embedded.class, _httpServletRequest);

		Embedded embedded = embeddedOptional.orElseThrow(
			() -> new VulcanDeveloperError.MustHaveProvider(Embedded.class));

		Optional<Language> optional = _providerManager.provideOptional(
			Language.class, _httpServletRequest);

		Language language = optional.orElseThrow(
			() -> new VulcanDeveloperError.MustHaveProvider(Language.class));

		Optional<ServerURL> serverURLOptional =
			_providerManager.provideOptional(
				ServerURL.class, _httpServletRequest);

		ServerURL serverURL = serverURLOptional.orElseThrow(
			() -> new VulcanDeveloperError.MustHaveProvider(ServerURL.class));

		_writeItems(
			pageMessageMapper, jsonObjectBuilder, page, fields, embedded,
			language, serverURL);

		_writeItemTotalCount(pageMessageMapper, jsonObjectBuilder, page);

		_writePageCount(pageMessageMapper, jsonObjectBuilder, page);

		_writePageURLs(pageMessageMapper, jsonObjectBuilder, page, serverURL);

		_writeCollectionURL(
			pageMessageMapper, jsonObjectBuilder, page, serverURL);

		pageMessageMapper.onFinish(jsonObjectBuilder, page, _httpHeaders);

		JsonObject jsonObject = jsonObjectBuilder.build();

		printWriter.println(jsonObject.toString());

		printWriter.close();
	}

	private String _getCollectionURL(Page<T> page, ServerURL serverURL) {
		Optional<String> optional = _writerHelper.getCollectionURLOptional(
			page, serverURL);

		Class<T> modelClass = page.getModelClass();

		return optional.orElseThrow(
			() -> new UnresolvableURI(modelClass.getName()));
	}

	private String _getPageURL(
		Page<T> page, ServerURL serverURL, int pageNumber, int itemsPerPage) {

		String url = _getCollectionURL(page, serverURL);

		return url + "?page=" + pageNumber + "&per_page=" + itemsPerPage;
	}

	private void _writeCollectionURL(
		PageMessageMapper<T> pageMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, Page<T> page,
		ServerURL serverURL) {

		String url = _getCollectionURL(page, serverURL);

		pageMessageMapper.mapCollectionURL(jsonObjectBuilder, url);
	}

	private <U, V> void _writeEmbeddedRelatedModel(
		PageMessageMapper<?> pageMessageMapper,
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder,
		RelatedModel<U, V> relatedModel, SingleModel<U> parentSingleModel,
		FunctionalList<String> parentEmbeddedPathElements, Fields fields,
		Embedded embedded, Language language, ServerURL serverURL) {

		_writerHelper.writeRelatedModel(
			relatedModel, parentSingleModel, parentEmbeddedPathElements,
			serverURL, fields, embedded,
			(singleModel, embeddedPathElements) -> {
				Class<V> modelClass = singleModel.getModelClass();

				_writerHelper.writeBooleanFields(
					singleModel.getModel(), modelClass, fields,
					(fieldName, value) ->
						pageMessageMapper.mapItemEmbeddedResourceBooleanField(
							pageJSONObjectBuilder, itemJSONObjectBuilder,
							embeddedPathElements, fieldName, value));

				_writerHelper.writeLocalizedStringFields(
					singleModel.getModel(), modelClass, fields, language,
					(fieldName, value) ->
						pageMessageMapper.mapItemEmbeddedResourceStringField(
							pageJSONObjectBuilder, itemJSONObjectBuilder,
							embeddedPathElements, fieldName, value));

				_writerHelper.writeNumberFields(
					singleModel.getModel(), modelClass, fields,
					(fieldName, value) ->
						pageMessageMapper.mapItemEmbeddedResourceNumberField(
							pageJSONObjectBuilder, itemJSONObjectBuilder,
							embeddedPathElements, fieldName, value));

				_writerHelper.writeStringFields(
					singleModel.getModel(), modelClass, fields,
					(fieldName, value) ->
						pageMessageMapper.mapItemEmbeddedResourceStringField(
							pageJSONObjectBuilder, itemJSONObjectBuilder,
							embeddedPathElements, fieldName, value));

				_writerHelper.writeLinks(
					modelClass, fields,
					(fieldName, link) ->
						pageMessageMapper.mapItemEmbeddedResourceLink(
							pageJSONObjectBuilder, itemJSONObjectBuilder,
							embeddedPathElements, fieldName, link));

				_writerHelper.writeTypes(
					modelClass,
					types -> pageMessageMapper.mapItemEmbeddedResourceTypes(
						pageJSONObjectBuilder, itemJSONObjectBuilder,
						embeddedPathElements, types));

				Optional<Representor<V, Identifier>> representorOptional =
					_collectionResourceManager.getRepresentorOptional(
						modelClass);

				representorOptional.ifPresent(
					representor -> {
						Map<String, BinaryFunction<V>> binaryFunctions =
							representor.getBinaryFunctions();

						_writerHelper.writeBinaries(
							binaryFunctions, singleModel, serverURL,
							(fieldName, value) ->
								pageMessageMapper.mapItemStringField(
									pageJSONObjectBuilder,
									itemJSONObjectBuilder, fieldName, value));

						List<RelatedModel<V, ?>> embeddedRelatedModels =
							representor.getEmbeddedRelatedModels();

						embeddedRelatedModels.forEach(
							embeddedRelatedModel -> _writeEmbeddedRelatedModel(
								pageMessageMapper, pageJSONObjectBuilder,
								itemJSONObjectBuilder, embeddedRelatedModel,
								singleModel, embeddedPathElements, fields,
								embedded, language, serverURL));

						List<RelatedModel<V, ?>> linkedRelatedModels =
							representor.getLinkedRelatedModels();

						linkedRelatedModels.forEach(
							linkedRelatedModel -> _writeLinkedRelatedModel(
								pageMessageMapper, pageJSONObjectBuilder,
								itemJSONObjectBuilder, linkedRelatedModel,
								singleModel, embeddedPathElements, fields,
								embedded, serverURL));

						Stream<RelatedCollection<V, ?>> stream =
							representor.getRelatedCollections();

						stream.forEach(
							relatedCollection -> _writeRelatedCollection(
								pageMessageMapper, pageJSONObjectBuilder,
								itemJSONObjectBuilder, relatedCollection,
								singleModel, embeddedPathElements, fields,
								serverURL));
					});
			},
			(url, embeddedPathElements, isEmbedded) -> {
				if (isEmbedded) {
					pageMessageMapper.mapItemEmbeddedResourceURL(
						pageJSONObjectBuilder, itemJSONObjectBuilder,
						embeddedPathElements, url);
				}
				else {
					pageMessageMapper.mapItemLinkedResourceURL(
						pageJSONObjectBuilder, itemJSONObjectBuilder,
						embeddedPathElements, url);
				}
			});
	}

	private void _writeItems(
		PageMessageMapper<T> pageMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, Page<T> page, Fields fields,
		Embedded embedded, Language language, ServerURL serverURL) {

		Collection<T> items = page.getItems();

		items.forEach(
			item -> {
				JSONObjectBuilder itemJSONObjectBuilder =
					new JSONObjectBuilder();

				Class<T> modelClass = page.getModelClass();

				pageMessageMapper.onStartItem(
					jsonObjectBuilder, itemJSONObjectBuilder, item, modelClass,
					_httpHeaders);

				_writerHelper.writeBooleanFields(
					item, modelClass, fields,
					(field, value) -> pageMessageMapper.mapItemBooleanField(
						jsonObjectBuilder, itemJSONObjectBuilder, field,
						value));

				_writerHelper.writeLocalizedStringFields(
					item, modelClass, fields, language,
					(field, value) -> pageMessageMapper.mapItemStringField(
						jsonObjectBuilder, itemJSONObjectBuilder, field,
						value));

				_writerHelper.writeNumberFields(
					item, modelClass, fields,
					(field, value) -> pageMessageMapper.mapItemNumberField(
						jsonObjectBuilder, itemJSONObjectBuilder, field,
						value));

				_writerHelper.writeStringFields(
					item, modelClass, fields,
					(field, value) -> pageMessageMapper.mapItemStringField(
						jsonObjectBuilder, itemJSONObjectBuilder, field,
						value));

				_writerHelper.writeLinks(
					modelClass, fields,
					(fieldName, link) -> pageMessageMapper.mapItemLink(
						jsonObjectBuilder, itemJSONObjectBuilder, fieldName,
						link));

				_writerHelper.writeTypes(
					modelClass,
					types -> pageMessageMapper.mapItemTypes(
						jsonObjectBuilder, itemJSONObjectBuilder, types));

				SingleModel<T> singleModel = new SingleModel<>(
					item, modelClass);

				Optional<String> optional = _writerHelper.getSingleURLOptional(
					singleModel, serverURL);

				optional.ifPresent(
					url -> pageMessageMapper.mapItemSelfURL(
						jsonObjectBuilder, itemJSONObjectBuilder, url));

				Optional<Representor<T, Identifier>> representorOptional =
					_collectionResourceManager.getRepresentorOptional(
						modelClass);

				representorOptional.ifPresent(
					representor -> {
						Map<String, BinaryFunction<T>> binaryFunctions =
							representor.getBinaryFunctions();

						_writerHelper.writeBinaries(
							binaryFunctions, singleModel, serverURL,
							(fieldName, value) ->
								pageMessageMapper.mapItemStringField(
									jsonObjectBuilder, itemJSONObjectBuilder,
									fieldName, value));

						List<RelatedModel<T, ?>> embeddedRelatedModels =
							representor.getEmbeddedRelatedModels();

						embeddedRelatedModels.forEach(
							embeddedRelatedModel -> _writeEmbeddedRelatedModel(
								pageMessageMapper, jsonObjectBuilder,
								itemJSONObjectBuilder, embeddedRelatedModel,
								singleModel, null, fields, embedded, language,
								serverURL));

						List<RelatedModel<T, ?>> linkedRelatedModels =
							representor.getLinkedRelatedModels();

						linkedRelatedModels.forEach(
							linkedRelatedModel -> _writeLinkedRelatedModel(
								pageMessageMapper, jsonObjectBuilder,
								itemJSONObjectBuilder, linkedRelatedModel,
								singleModel, null, fields, embedded,
								serverURL));

						Stream<RelatedCollection<T, ?>> stream =
							representor.getRelatedCollections();

						stream.forEach(
							relatedCollection -> _writeRelatedCollection(
								pageMessageMapper, jsonObjectBuilder,
								itemJSONObjectBuilder, relatedCollection,
								singleModel, null, fields, serverURL));
					});

				pageMessageMapper.onFinishItem(
					jsonObjectBuilder, itemJSONObjectBuilder, item, modelClass,
					_httpHeaders);
			});
	}

	private void _writeItemTotalCount(
		PageMessageMapper<T> pageMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, Page<T> page) {

		pageMessageMapper.mapItemTotalCount(
			jsonObjectBuilder, page.getTotalCount());
	}

	private <U, V> void _writeLinkedRelatedModel(
		PageMessageMapper<?> pageMessageMapper,
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder,
		RelatedModel<U, V> relatedModel, SingleModel<U> parentSingleModel,
		FunctionalList<String> parentEmbeddedPathElements, Fields fields,
		Embedded embedded, ServerURL serverURL) {

		_writerHelper.writeLinkedRelatedModel(
			relatedModel, parentSingleModel, parentEmbeddedPathElements,
			serverURL, fields, embedded,
			(url, embeddedPathElements) ->
				pageMessageMapper.mapItemLinkedResourceURL(
					pageJSONObjectBuilder, itemJSONObjectBuilder,
					embeddedPathElements, url));
	}

	private void _writePageCount(
		PageMessageMapper<T> pageMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, Page<T> page) {

		Collection<T> items = page.getItems();

		pageMessageMapper.mapPageCount(jsonObjectBuilder, items.size());
	}

	private void _writePageURLs(
		PageMessageMapper<T> pageMessageMapper,
		JSONObjectBuilder jsonObjectBuilder, Page<T> page,
		ServerURL serverURL) {

		pageMessageMapper.mapCurrentPageURL(
			jsonObjectBuilder,
			_getPageURL(
				page, serverURL, page.getPageNumber(), page.getItemsPerPage()));

		pageMessageMapper.mapFirstPageURL(
			jsonObjectBuilder,
			_getPageURL(page, serverURL, 1, page.getItemsPerPage()));

		if (page.hasPrevious()) {
			pageMessageMapper.mapPreviousPageURL(
				jsonObjectBuilder,
				_getPageURL(
					page, serverURL, page.getPageNumber() - 1,
					page.getItemsPerPage()));
		}

		if (page.hasNext()) {
			pageMessageMapper.mapNextPageURL(
				jsonObjectBuilder,
				_getPageURL(
					page, serverURL, page.getPageNumber() + 1,
					page.getItemsPerPage()));
		}

		pageMessageMapper.mapLastPageURL(
			jsonObjectBuilder,
			_getPageURL(
				page, serverURL, page.getLastPageNumber(),
				page.getItemsPerPage()));
	}

	private <U, V> void _writeRelatedCollection(
		PageMessageMapper<?> pageMessageMapper,
		JSONObjectBuilder pageJSONObjectBuilder,
		JSONObjectBuilder itemJSONObjectBuilder,
		RelatedCollection<U, V> relatedCollection,
		SingleModel<U> parentSingleModel,
		FunctionalList<String> parentEmbeddedPathElements, Fields fields,
		ServerURL serverURL) {

		_writerHelper.writeRelatedCollection(
			relatedCollection, parentSingleModel, parentEmbeddedPathElements,
			serverURL, fields,
			(url, embeddedPathElements) ->
				pageMessageMapper.mapItemLinkedResourceURL(
					pageJSONObjectBuilder, itemJSONObjectBuilder,
					embeddedPathElements, url));
	}

	@Reference
	private CollectionResourceManager _collectionResourceManager;

	@Context
	private HttpHeaders _httpHeaders;

	@Context
	private HttpServletRequest _httpServletRequest;

	@Reference(cardinality = AT_LEAST_ONE, policyOption = GREEDY)
	private List<PageMessageMapper<T>> _pageMessageMappers;

	@Reference
	private ProviderManager _providerManager;

	@Context
	private ResourceInfo _resourceInfo;

	@Reference
	private WriterHelper _writerHelper;

}