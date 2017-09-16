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
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.service.CPDefinitionService;
import com.liferay.commerce.product.service.CPInstanceService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
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
 * href="http://schema.org/IndividualProduct">IndividualProduct</a> resource through
 * a web API. <p> The resources are mapped from the internal {@link CPInstance}
 * model.
 *
 * @author Javier Gamarra
 */
@Component(immediate = true)
public class IndividualProductResource
	implements Resource<CPInstance, LongIdentifier> {

	@Override
	public Representor<CPInstance, LongIdentifier> buildRepresentor(
		RepresentorBuilder<CPInstance, LongIdentifier> representorBuilder) {

		return representorBuilder.identifier(
			assetCategory -> assetCategory::getCPDefinitionId
		).addField(
			"name", CPInstance::getModelClassName
		).addType(
			"IndividualProduct"
		).build();
	}

	@Override
	public String getPath() {
		return "individual-products";
	}

	@Override
	public Routes<CPInstance> routes(
		RoutesBuilder<CPInstance, LongIdentifier> routesBuilder) {

		return routesBuilder.collectionPageItemGetter(
			this::_getCPInstance
		).collectionPageGetter(
			this::_getPageItems, LongIdentifier.class
		).build();
	}

	private CPInstance _getCPInstance(LongIdentifier cpInstanceLongIdentifier) {
		try {
			return _cpInstanceService.getCPInstance(
				cpInstanceLongIdentifier.getId());
		}
		catch (NoSuchEntryException | PrincipalException e) {
			throw new NotFoundException(
				"Unable to get CPInstance " + cpInstanceLongIdentifier.getId(),
				e);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private PageItems<CPInstance> _getPageItems(
		Pagination pagination, LongIdentifier longIdentifier) {

		try {
			List<CPInstance> cpDefinitions =
				_cpInstanceService.getCPDefinitionInstances(
					longIdentifier.getId(), WorkflowConstants.STATUS_APPROVED,
					pagination.getStartPosition(), pagination.getEndPosition(),
					null);

			int count = _cpInstanceService.getCPDefinitionInstancesCount(
				longIdentifier.getId(), WorkflowConstants.STATUS_APPROVED);

			return new PageItems<>(cpDefinitions, count);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	@Reference
	private CPDefinitionService _cpDefinitionService;

	@Reference
	private CPInstanceService _cpInstanceService;

}