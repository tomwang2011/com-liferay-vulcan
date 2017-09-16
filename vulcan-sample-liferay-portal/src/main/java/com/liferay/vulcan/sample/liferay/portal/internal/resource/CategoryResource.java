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

package com.liferay.vulcan.sample.liferay.portal.internal.resource;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetCategoryModel;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryService;
import com.liferay.asset.kernel.service.AssetVocabularyService;
import com.liferay.blogs.kernel.exception.NoSuchEntryException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.service.CPDefinitionService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.vulcan.pagination.PageItems;
import com.liferay.vulcan.pagination.Pagination;
import com.liferay.vulcan.resource.Representor;
import com.liferay.vulcan.resource.Resource;
import com.liferay.vulcan.resource.Routes;
import com.liferay.vulcan.resource.builder.RepresentorBuilder;
import com.liferay.vulcan.resource.builder.RoutesBuilder;
import com.liferay.vulcan.resource.identifier.LongIdentifier;

import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides all the necessary information to expose <a
 * href="http://schema.org/Category">Category</a> resource through
 * a web API. <p> The resources are mapped from the internal {@link AssetCategory}
 * model.
 *
 * @author Javier Gamarra
 */
@Component(immediate = true)
public class CategoryResource
	implements Resource<AssetCategory, LongIdentifier> {

	@Override
	public Representor<AssetCategory, LongIdentifier> buildRepresentor(
		RepresentorBuilder<AssetCategory, LongIdentifier> representorBuilder) {

		return representorBuilder.identifier(
			assetCategory -> assetCategory::getCategoryId
		).addField(
			"name", AssetCategoryModel::getName
		).addRelatedCollection(
			"products", CPDefinition.class,
			x -> (LongIdentifier)x::getCategoryId
		).addType(
			"Category"
		).build();
	}

	@Override
	public String getPath() {
		return "categories";
	}

	@Override
	public Routes<AssetCategory> routes(
		RoutesBuilder<AssetCategory, LongIdentifier> routesBuilder) {

		return routesBuilder.collectionPageItemGetter(
			this::_getAssetCategory
		).collectionPageGetter(
			this::_getPageItems, LongIdentifier.class
		).build();
	}

	private AssetCategory _getAssetCategory(
		LongIdentifier assetCategoryLongIdentifier) {

		try {
			return _assetCategoryService.getCategory(
				assetCategoryLongIdentifier.getId());
		}
		catch (NoSuchEntryException | PrincipalException e) {
			throw new NotFoundException(
				"Unable to get category " + assetCategoryLongIdentifier.getId(),
				e);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private PageItems<AssetCategory> _getPageItems(
		Pagination pagination, LongIdentifier assetCategoryLongIdentifier) {

		try {
			AssetVocabulary assetVocabulary =
				_assetVocabularyService.getVocabulary(
					assetCategoryLongIdentifier.getId());

			List<AssetCategory> assetCategories =
				_assetCategoryService.getVocabularyRootCategories(
					assetVocabulary.getGroupId(),
					assetVocabulary.getVocabularyId(),
					pagination.getStartPosition(), pagination.getEndPosition(),
					null);

			int count = _assetCategoryService.getVocabularyRootCategoriesCount(
				assetVocabulary.getGroupId(),
				assetVocabulary.getVocabularyId());

			return new PageItems<>(assetCategories, count);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	@Reference
	private AssetCategoryService _assetCategoryService;

	@Reference
	private AssetVocabularyService _assetVocabularyService;

	@Reference
	private CPDefinitionService _cpDefinitionService;

}