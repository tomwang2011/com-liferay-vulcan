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
 * href="http://schema.org/Product">Product</a> resource through
 * a web API. <p> The resources are mapped from the internal {@link CPDefinition}
 * model.
 *
 * @author Javier Gamarra
 */
@Component(immediate = true)
public class ProductResource implements Resource<CPDefinition, LongIdentifier> {

	@Override
	public Representor<CPDefinition, LongIdentifier> buildRepresentor(
		RepresentorBuilder<CPDefinition, LongIdentifier> representorBuilder) {

		return representorBuilder.identifier(
			assetCategory -> assetCategory::getCPDefinitionId
		).addField(
			"name", CPDefinition::getModelClassName
		).addType(
			"Product"
		).build();
	}

	@Override
	public String getPath() {
		return "products";
	}

	@Override
	public Routes<CPDefinition> routes(
		RoutesBuilder<CPDefinition, LongIdentifier> routesBuilder) {

		return routesBuilder.collectionPageItemGetter(
			this::_getCPDefinition
		).collectionPageGetter(
			this::_getPageItems, LongIdentifier.class
		).build();
	}

	private CPDefinition _getCPDefinition(
		LongIdentifier cpDefinitionLongIdentifier) {

		try {
			return _cpDefinitionService.getCPDefinition(
				cpDefinitionLongIdentifier.getId());
		}
		catch (NoSuchEntryException | PrincipalException e) {
			throw new NotFoundException(
				"Unable to get product " + cpDefinitionLongIdentifier.getId(),
				e);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private PageItems<CPDefinition> _getPageItems(
		Pagination pagination, LongIdentifier longIdentifier) {

		List<CPDefinition> cpDefinitions =
			_cpDefinitionService.getCPDefinitionsByCategoryId(
				longIdentifier.getId(), pagination.getStartPosition(),
				pagination.getEndPosition());

		int count = _cpDefinitionService.getCPDefinitionsCountByCategoryId(
			longIdentifier.getId());

		return new PageItems<>(cpDefinitions, count);
	}

	@Reference
	private CPDefinitionService _cpDefinitionService;

}