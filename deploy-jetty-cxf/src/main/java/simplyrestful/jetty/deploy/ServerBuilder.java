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

package simplyrestful.jetty.deploy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.apache.cxf.jaxrs.provider.MultipartProvider;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationFeature;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInvoker;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationOutInterceptor;
import org.apache.cxf.jaxrs.validation.ValidationExceptionMapper;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import simplyrestful.api.framework.core.DefaultWebResource;
import simplyrestful.api.framework.core.providers.JacksonHALJsonProvider;
import simplyrestful.api.framework.core.providers.ObjectMapperProvider;
import simplyrestful.api.framework.core.servicedocument.WebResourceRoot;
import simplyrestful.api.framework.resources.HALResource;


/**
 * This represents the API server that runs the SimplyRESTful API.
 *
 * This will create a pre-configured Jetty instance that will run any JAX-RS Web Resource you provide it with. This is
 * intended as a quick way to get your SimplyRESTful API running and uses manual configuration to deploy Apache CXF.
 *
 * @author RiaasM
 *
 */
public class ServerBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger("simplyrestful.jetty.deploy.APIServer");
	private String address = "http://localhost:9000";
	private List<Class<? extends DefaultWebResource<? extends HALResource>>> webResources = new ArrayList<>();
	private List<Object> providers = new ArrayList<>();

	/**
	 * Configure the address used by the server
	 *
	 * @param address is the address which should be used by the server
	 * @return the builder object
	 */
	public ServerBuilder withAddress(String address) {
		this.address = address;
		return this;
	}

	/**
	 * Add a SimplyRESTful JAX-RS web resource to the server
	 *
	 * @param <T> is the HAL-based resource to which the Web Resource provides acces.
	 * @param webResource is the SimplyRESTful JAX-RS web resource to add to the server
	 * @return the builder object
	 */
	public <T extends HALResource> ServerBuilder withWebResource(
			Class<? extends DefaultWebResource<T>> webResource) {
		webResources.add(webResource);
		return this;
	}

	/**
	 * Add a JAX-RS provider to the server
	 *
	 * @param provider is the JAX-RS provider that should be added to the server
	 * @return the builder object
	 */
	public ServerBuilder withProvider(Object provider) {
		providers.add(provider);
		return this;
	}

    /**
     * Create the API server with the JAX-RS API Web Resources and address specified in the builder.
     *
     * The Web Resource will have its lifecycle set to singleton. If this is not possible, e.g. if no
     * instance can be created for the Web Resource, it will fall back to a per-request lifecycle.
     *
     * @throws SecurityException when the constructor of the web resource class can not be used
     * @throws NoSuchMethodException when the constructor of the web resource class does not exist
     * @throws InvocationTargetException when the new instance of the web resource class can not be created
     * @throws IllegalArgumentException when the new instance of the web resource class can not be created
     * @throws IllegalAccessException when the new instance of the web resource class can not be created
     * @throws InstantiationException when the new instance of the web resource class can not be created
     * @return the CXF server, as configured in the builder
     */
    public Server build() throws ReflectiveOperationException {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        // Configure resources
        List<Class<?>> resourceClasses = new ArrayList<>();
        resourceClasses.add(WebResourceRoot.class);
        for (Class<? extends DefaultWebResource<? extends HALResource>> webResource: webResources){
			resourceClasses.add(webResource);
        }
        sf.setResourceClasses(resourceClasses);
        // Configure address
        if (address != null && !address.isEmpty()){
        	sf.setAddress(address);
        }
        // Register transport factory
        BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(sf.getBus());
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
        // Configure and enable Swagger for generation swagger.json
        OpenApiFeature swagger = new OpenApiFeature();
        swagger.setPrettyPrint(true);
        sf.getFeatures().add(swagger);
        // Configure and enable Bean Validation along with registering all providers
        sf.setInvoker(new JAXRSBeanValidationInvoker());
        sf.getFeatures().add(new JAXRSBeanValidationFeature());
        providers.addAll(Arrays.asList(
        		ValidationExceptionMapper.class,
        		MultipartProvider.class,
        		ObjectMapperProvider.class,
        		JacksonHALJsonProvider.class,
        		JacksonJsonProvider.class,
        		OpenApiResource.class,
        		AcceptHeaderOpenApiResource.class));
        sf.setProviders(providers);
        sf.setInInterceptors(Arrays.< Interceptor< ? extends Message > >asList(new JAXRSBeanValidationInInterceptor()));
        sf.setOutInterceptors(Arrays.< Interceptor< ? extends Message > >asList(new JAXRSBeanValidationOutInterceptor()));
        // Start the server
        LOGGER.info("Server ready...");
        return sf.create();
    }
}
