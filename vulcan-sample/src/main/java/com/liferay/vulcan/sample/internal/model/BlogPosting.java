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

package com.liferay.vulcan.sample.internal.model;

import com.github.javafaker.Book;
import com.github.javafaker.DateAndTime;
import com.github.javafaker.Faker;
import com.github.javafaker.Lorem;
import com.github.javafaker.service.RandomService;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Instances of this class represents a blog posting. This is a mock class for
 * sample purposes. It contains methods for retrieving/updating/deleting blog
 * posts and a in-memory database with fake data.
 *
 * @author Alejandro Hernández
 * @review
 */
public class BlogPosting {

	/**
	 * Adds a new {@code BlogPosting} to the database.
	 *
	 * @param  title the title of the blog posting.
	 * @param  subtitle the subtitle of the blog posting.
	 * @param  content the content of the blog posting.
	 * @param  creatorId the ID of the creator of this blog posting.
	 * @return the added {@code BlogPosting}.
	 * @review
	 */
	public static BlogPosting addBlogPosting(
		String title, String subtitle, String content, long creatorId) {

		long id = _count.incrementAndGet();

		BlogPosting blogPosting = new BlogPosting(
			id, title, subtitle, content, creatorId, new Date(), new Date());

		_blogPostings.put(id, blogPosting);

		return blogPosting;
	}

	/**
	 * Deletes a {@code BlogPosting} with a certain {@code ID} from the
	 * database.
	 *
	 * @param  id the ID of the blog posting to delete.
	 * @review
	 */
	public static void deleteBlogPosting(long id) {
		_blogPostings.remove(id);
	}

	/**
	 * Returns a {@code BlogPosting} with a certain {@code ID} from the database
	 * if present. Returns {@code Optional#empty()} otherwise.
	 *
	 * @param  id the ID of the blog posting to retrieve.
	 * @return the {@code BlogPosting} for the requested ID if present; {@code
	 *         Optional#empty()} otherwise.
	 * @review
	 */
	public static Optional<BlogPosting> getBlogPosting(long id) {
		BlogPosting blogPosting = _blogPostings.get(id);

		return Optional.ofNullable(blogPosting);
	}

	/**
	 * Return the total number of blog postings in the database.
	 *
	 * @return the total number of blog postings in the database.
	 * @review
	 */
	public static int getBlogPostingCount() {
		return _blogPostings.size();
	}

	/**
	 * Returns a page of {@code BlogPosting} from the database.
	 *
	 * @param  start the start position.
	 * @param  end the end position.
	 * @return the list of blog postings between {@code start} and {@code end}.
	 * @review
	 */
	public static List<BlogPosting> getBlogPostings(int start, int end) {
		Collection<BlogPosting> blogPostings = _blogPostings.values();

		Stream<BlogPosting> stream = blogPostings.stream();

		return stream.skip(
			start
		).limit(
			end
		).collect(
			Collectors.toList()
		);
	}

	/**
	 * Updates a {@code BlogPosting} with a certain {@code ID} in the database,
	 * if present.
	 *
	 * @param  id the ID of the blog posting to update.
	 * @param  title the new title for the blog posting.
	 * @param  subtitle the new subtitle for the blog posting.
	 * @param  content the new content for the blog posting.
	 * @param  creatorId the ID of the new creator for the blog posting.
	 * @return the updated {@code BlogPosting} if present; {@code
	 *         Optional#empty()} otherwise.
	 * @review
	 */
	public static Optional<BlogPosting> updateBlogPosting(
		long id, String title, String subtitle, String content,
		long creatorId) {

		BlogPosting blogPosting = _blogPostings.get(id);

		if (blogPosting == null) {
			return Optional.empty();
		}

		Date createDate = blogPosting.getCreateDate();

		blogPosting = new BlogPosting(
			id, title, subtitle, content, creatorId, createDate, new Date());

		_blogPostings.put(id, blogPosting);

		return Optional.of(blogPosting);
	}

	/**
	 * Returns the content of this {@code BlogPosting}.
	 *
	 * @return the content of the blog posting.
	 * @review
	 */
	public String getContent() {
		return _content;
	}

	/**
	 * Returns the create date of this {@code BlogPosting}.
	 *
	 * @return the create date of the blog posting.
	 * @review
	 */
	public Date getCreateDate() {
		return _createDate;
	}

	/**
	 * Returns the ID of the creator of this {@code BlogPosting}.
	 *
	 * @return the ID of the creator the blog posting.
	 * @review
	 */
	public long getCreatorId() {
		return _creatorId;
	}

	/**
	 * Returns the ID of this {@code BlogPosting}.
	 *
	 * @return the ID of the blog posting.
	 * @review
	 */
	public long getId() {
		return _id;
	}

	/**
	 * Returns the modified date of this {@code BlogPosting}.
	 *
	 * @return the modified date of the blog posting.
	 * @review
	 */
	public Date getModifiedDate() {
		return _modifiedDate;
	}

	/**
	 * Returns the subtitle of this {@code BlogPosting}.
	 *
	 * @return the subtitle of the blog posting.
	 * @review
	 */
	public String getSubtitle() {
		return _subtitle;
	}

	/**
	 * Returns the title of this {@code BlogPosting}.
	 *
	 * @return the title of the blog posting.
	 * @review
	 */
	public String getTitle() {
		return _title;
	}

	private BlogPosting(
		long id, String title, String subtitle, String content, long creatorId,
		Date createDate, Date modifiedDate) {

		_id = id;
		_title = title;
		_subtitle = subtitle;
		_content = content;
		_creatorId = creatorId;
		_createDate = createDate;
		_modifiedDate = modifiedDate;
	}

	private static Map<Long, BlogPosting> _blogPostings;
	private static final AtomicLong _count = new AtomicLong(30);

	static {
		_blogPostings = new HashMap<>();

		for (long i = 0; i < 42; i++) {
			Faker faker = new Faker();

			Book book = faker.book();

			String title = book.title();

			Lorem lorem = faker.lorem();

			String subtitle = lorem.sentence();

			List<String> paragraphs = lorem.paragraphs(5);

			Stream<String> stream = paragraphs.stream();

			String content = stream.map(
				paragraph -> "<p>" + paragraph + "</p>"
			).collect(
				Collectors.joining()
			);

			RandomService randomService = faker.random();

			int creatorId = randomService.nextInt(Person.getPeopleCount());

			DateAndTime dateAndTime = faker.date();

			Date date = dateAndTime.past(400, TimeUnit.DAYS);

			BlogPosting blogPosting = new BlogPosting(
				i, title, subtitle, content, creatorId, date, date);

			_blogPostings.put(i, blogPosting);
		}
	}

	private final String _content;
	private final Date _createDate;
	private final long _creatorId;
	private final long _id;
	private final Date _modifiedDate;
	private final String _subtitle;
	private final String _title;

}