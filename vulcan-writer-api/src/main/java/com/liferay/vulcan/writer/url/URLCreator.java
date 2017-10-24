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

package com.liferay.vulcan.writer.url;

import com.liferay.vulcan.uri.Path;
import com.liferay.vulcan.url.ServerURL;

/**
 * This class manages the creation of URLs and has all the necessary information
 * about them.
 *
 * <p>
 * This class shouldn't be instantiated.
 * </p>
 *
 * @author Alejandro Hernández
 * @review
 */
public final class URLCreator {

	/**
	 * Returns the URL for a binary resource.
	 *
	 * @param  serverURL the server URL.
	 * @param  binaryId the ID of the binary resource.
	 * @param  path the {@code Path} of the resource.
	 * @return the URL for a binary resource.
	 * @review
	 */
	public static String createBinaryURL(
		ServerURL serverURL, String binaryId, Path path) {

		return serverURL.getServerURL() + "/b" + path.asURI() + "/" + binaryId;
	}

	/**
	 * Returns the URL for a related collection.
	 *
	 * @param  serverURL the server URL.
	 * @param  path of the single resource.
	 * @param  name the name of the related resource.
	 * @return the related collection URL.
	 * @review
	 */
	public static String createRelatedCollectionURL(
		ServerURL serverURL, Path path, String name) {

		return serverURL.getServerURL() + "/p" + path.asURI() + "/" + name;
	}

	/**
	 * Returns the URL to the resource of a certain model.
	 *
	 * @param  serverURL the server URL.
	 * @param  path the {@code Path} of the resource.
	 * @return the single URL for the {@code CollectionResource}.
	 * @review
	 */
	public static String createSingleURL(ServerURL serverURL, Path path) {
		String serverURLString = serverURL.getServerURL();

		return serverURLString + "/p" + path.asURI();
	}

	private URLCreator() {
		throw new UnsupportedOperationException();
	}

}