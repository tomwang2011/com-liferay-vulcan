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

package com.liferay.vulcan.sample.liferay.portal.internal;

import com.liferay.commerce.model.CommerceCart;
import com.liferay.commerce.util.CommerceCartHelper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.vulcan.provider.Provider;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.osgi.service.component.annotations.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ServerErrorException;

/**
 * @author Alejandro Hernández
 */
@Component(immediate = true)
public class CurrentCartProvider implements Provider<CommerceCart> {

	@Override
	public CommerceCart createContext(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		try {
			return _commerceCartHelper.getCurrentCommerceCart(
				httpServletRequest, httpServletResponse);
		} catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	@Reference
	private CommerceCartHelper _commerceCartHelper;
}
