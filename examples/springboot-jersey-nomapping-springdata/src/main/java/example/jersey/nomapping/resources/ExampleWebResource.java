/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package example.jersey.nomapping.resources;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import simplyrestful.api.framework.core.AdditionalMediaTypes;
import simplyrestful.springdata.resources.SpringDataWebResource;

@Named
@Path("/resources")
@Api(value = "Example Resources")
@Produces(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\""+ExampleResource.EXAMPLE_PROFILE_STRING + "\"")
@Consumes(AdditionalMediaTypes.APPLICATION_HAL_JSON + "; profile=\""+ExampleResource.EXAMPLE_PROFILE_STRING + "\"")
public class ExampleWebResource extends SpringDataWebResource<ExampleResource> {
	@Inject
	public ExampleWebResource(ExampleRepository exampleRepo) {
		super(exampleRepo);
	}
}
