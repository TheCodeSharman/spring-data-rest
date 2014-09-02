/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.rest.webmvc.mongodb;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.AbstractWebIntegrationTests;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

/**
 * Integration tests for MongoDB repositories.
 * 
 * @author Oliver Gierke
 */
@ContextConfiguration(classes = MongoDbRepositoryConfig.class)
@Ignore
public class MongoWebTests extends AbstractWebIntegrationTests {

	@Autowired ProfileRepository repository;
	@Autowired UserRepository userRepository;

	@Before
	public void populateProfiles() {

		Profile twitter = new Profile();
		twitter.setPerson(1L);
		twitter.setType("Twitter");

		Profile linkedIn = new Profile();
		linkedIn.setPerson(1L);
		linkedIn.setType("LinkedIn");

		repository.save(Arrays.asList(twitter, linkedIn));

		Address address = new Address();
		address.street = "Foo";
		address.zipCode = "Bar";

		User user = new User();
		user.firstname = "Oliver";
		user.lastname = "Gierke";
		user.address = address;

		userRepository.save(user);
	}

	@After
	public void cleanUp() {
		repository.deleteAll();
		userRepository.deleteAll();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.rest.webmvc.AbstractWebIntegrationTests#expectedRootLinkRels()
	 */
	@Override
	protected Iterable<String> expectedRootLinkRels() {
		return Arrays.asList("profiles", "users");
	}

	@Test
	public void foo() throws Exception {

		Link profileLink = discoverUnique("profiles");
		follow(profileLink).//
				andExpect(jsonPath("$._embedded.profiles").value(hasSize(2)));
	}

	@Test
	public void rendersEmbeddedDocuments() throws Exception {

		Link usersLink = discoverUnique("users");
		Link userLink = assertHasContentLinkWithRel("self", request(usersLink));
		follow(userLink).//
				andDo(MockMvcResultHandlers.print()). //
				andExpect(jsonPath("$.address.zipCode").value(is(notNullValue())));
	}

	/**
	 * @see DATAREST-247
	 */
	@Test
	public void executeQueryMethodWithPrimitiveReturnType() throws Exception {

		Link profiles = discoverUnique("profiles");
		Link profileSearches = discoverUnique(profiles, "search");
		Link countByTypeLink = discoverUnique(profileSearches, "countByType");

		assertThat(countByTypeLink.isTemplated(), is(true));
		assertThat(countByTypeLink.getVariableNames(), hasItem("type"));

		MockHttpServletResponse response = request(countByTypeLink.expand("Twitter"));
		assertThat(response.getContentAsString(), is("1"));
	}
}
