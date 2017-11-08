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

package com.liferay.vulcan.test.message;

import com.liferay.vulcan.result.APIError;

import java.util.Optional;

/**
 * Instances of this class represent a mock {@link APIError} that can be used to
 * test an {@link com.liferay.vulcan.message.json.ErrorMessageMapper}.
 *
 * @author Alejandro Hernández
 * @review
 */
public class MockAPIError implements APIError {

	@Override
	public Optional<String> getDescription() {
		return Optional.of("A description");
	}

	@Override
	public Exception getException() {
		return new IllegalArgumentException("A message");
	}

	@Override
	public int getStatusCode() {
		return 404;
	}

	@Override
	public String getTitle() {
		return "A title";
	}

	@Override
	public String getType() {
		return "A type";
	}

}