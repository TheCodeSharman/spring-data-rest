/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.data.rest.webmvc.support;

import java.io.Serializable;

import org.springframework.core.MethodParameter;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.BaseUri;
import org.springframework.data.rest.webmvc.ResourceMetadataHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.util.UriUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link HandlerMethodArgumentResolver} to resolve entity ids for injection int handler method arguments annotated with
 * {@link BackendId}.
 * 
 * @author Oliver Gierke
 */
public class BackendIdHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private final ResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver;
	private final BaseUri baseUri;

	/**
	 * Creates a new {@link BackendIdHandlerMethodArgumentResolver} for the given {@link BackendIdConverter}s and
	 * {@link ResourceMetadataHandlerMethodArgumentResolver}.
	 * 
	 * @param resourceMetadataResolver the resolver to obtain {@link ResourceMetadata} from, must not be {@literal null}.
	 * @param baseUri must not be {@literal null}.
	 */
	public BackendIdHandlerMethodArgumentResolver(ResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver,
			BaseUri baseUri) {

		Assert.notNull(resourceMetadataResolver, "ResourceMetadata resolver must not be null!");
		Assert.notNull(baseUri, "BaseUri must not be null!");

		this.resourceMetadataResolver = resourceMetadataResolver;
		this.baseUri = baseUri;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver#supportsParameter(org.springframework.core.MethodParameter)
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(BackendId.class);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver#resolveArgument(org.springframework.core.MethodParameter, org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.context.request.NativeWebRequest, org.springframework.web.bind.support.WebDataBinderFactory)
	 */
	@Override
	public String resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest request, WebDataBinderFactory binderFactory) throws Exception {

		Class<?> parameterType = parameter.getParameterType();

		if (!parameterType.equals(Serializable.class)) {
			throw new IllegalArgumentException(String.format(
					"Method parameter for @%s must be of type %s! Got %s for method %s.", BackendId.class.getSimpleName(),
					Serializable.class.getSimpleName(), parameterType.getSimpleName(), parameter.getMethod()));
		}

		ResourceMetadata metadata = resourceMetadataResolver.resolveArgument(parameter, mavContainer, request,
				binderFactory);

		if (metadata == null) {
			throw new IllegalArgumentException("Could not obtain ResourceMetadata for request " + request);
		}

		String lookupPath = baseUri.getRepositoryLookupPath(request);
		return UriUtils.findMappingVariable("id", parameter, lookupPath);
	}
}
