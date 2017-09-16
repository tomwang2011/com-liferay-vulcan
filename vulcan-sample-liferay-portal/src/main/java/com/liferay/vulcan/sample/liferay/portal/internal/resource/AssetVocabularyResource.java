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
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetVocabularyService;
import com.liferay.blogs.kernel.model.BlogsEntry;
import com.liferay.portal.kernel.exception.NoSuchGroupException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.vulcan.pagination.PageItems;
import com.liferay.vulcan.pagination.Pagination;
import com.liferay.vulcan.resource.Representor;
import com.liferay.vulcan.resource.Resource;
import com.liferay.vulcan.resource.Routes;
import com.liferay.vulcan.resource.builder.RepresentorBuilder;
import com.liferay.vulcan.resource.builder.RoutesBuilder;
import com.liferay.vulcan.resource.identifier.LongIdentifier;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.swing.*;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Alejandro Hernández
 */
@Component(immediate = true)
public class AssetVocabularyResource implements
	Resource<AssetVocabulary, LongIdentifier> {

	@Reference
	private GroupLocalService _groupLocalService;

	@Override
	public Representor<AssetVocabulary, LongIdentifier> buildRepresentor(
		RepresentorBuilder<AssetVocabulary, LongIdentifier>
			representorBuilder) {

		return representorBuilder.identifier(
			assetVocabulary -> assetVocabulary::getVocabularyId
		).addBidirectionalModel(
			"group", "asset-vocabularies", Group.class, this::_getGroupOptional,
			group -> (LongIdentifier) group::getGroupId
		).addField(
			"name", AssetVocabulary::getName
		).addRelatedCollection(
			"categories", AssetCategory.class,
			assetVocabulary -> (LongIdentifier)assetVocabulary::getVocabularyId
		).build();
	}

	private Optional<Group> _getGroupOptional(AssetVocabulary assetVocabulary) {
		try {
			return Optional.of(
				_groupLocalService.getGroup(assetVocabulary.getGroupId()));
		}
		catch (NoSuchGroupException nsge) {
			throw new NotFoundException(
				"Unable to get group " + assetVocabulary.getGroupId(), nsge);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	@Override
	public String getPath() {
		return "asset-vocabulary";
	}

	@Override
	public Routes<AssetVocabulary> routes(
		RoutesBuilder<AssetVocabulary, LongIdentifier> routesBuilder) {

		return routesBuilder.collectionPageGetter(
			this::_getPageItems, LongIdentifier.class
		).build();
	}

	private PageItems<AssetVocabulary> _getPageItems(
		Pagination pagination, LongIdentifier groupLongIdentifier) {

		Collection<AssetVocabulary> items =
			_assetVocabularyService.getGroupVocabularies(
				groupLongIdentifier.getId(), pagination.getStartPosition(),
				pagination.getEndPosition(), null);

		int itemCount = _assetVocabularyService.getGroupVocabulariesCount(
			groupLongIdentifier.getId());

		return new PageItems<>(items, itemCount);
	}

	@Reference
	private AssetVocabularyService _assetVocabularyService;
}
