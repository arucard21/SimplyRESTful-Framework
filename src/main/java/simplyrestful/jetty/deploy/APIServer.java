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

import java.util.ArrayList;

import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.ext.search.SearchContextProvider;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.provider.MultipartProvider;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationFeature;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Lists;

import dk.nykredit.jackson.dataformat.hal.HALMapper;
import simplyrestful.api.framework.core.ApiEndpointBase;
import simplyrestful.api.framework.core.hal.HalResource;

/**
 * Start the API server.
 *
 * This will launch a pre-configured Jetty instance that will run any endpoints you provide it with.This is intended
 * as a quick way to get your SimplyRESTful API endpoint running and uses manual configuration to deploy Apache CXF.
 *
 * The server is started at http://localhost:9000 (unless a different address is provided). The API endpoints are made
 * available relative to the root, where the relative path is defined by the @Path annotation on the endpoint itself.
 *
 * The server will automatically generate a swagger.json file which is made available directly at the root (e.g.
 * http://localhost:9000/swagger.json). The Swagger-UI tool is also included and can be found at the relative path,
 * from the root, "/api-docs". By default, you'll see the standard Petstore example for Swagger-UI, but you can use
 * the generated swagger.json right away by using the following URL "/api-docs?url=/swagger.json".
 *
 * You can deploy your SimplyRESTful API on many different application servers (e.g. Tomcat, Glassfish, JBoss) in many
 * different ways (including Spring Boot). Please refer to the JAX-RS Deployment documentation of Apache CXF for more
 * information on how to do this (available at https://cxf.apache.org/docs/jax-rs-deployment.html).
 *
 * @author RiaasM
 *
 */
public class APIServer {
	public static Logger LOGGER = LoggerFactory.getLogger("simplyrestful.jetty.deploy.APIServer");
	/**
	 * Start the server on a specific address.
	 *
	 * @param address is the address on which the server should run. If empty, the server will be started on random port on localhost
	 * @param apiEndpoints is a list of the API endpoints that should served.
	 * @throws IllegalAccessException if the APIEndpoint class is not accessible.
	 * @throws InstantiationException if this APIEndpoint class could not be instantiated.
	 */
    private APIServer(String address, Class<? extends ApiEndpointBase<? extends HalResource>>... apiEndpoints) throws InstantiationException, IllegalAccessException{
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(apiEndpoints);
        ArrayList<ResourceProvider> resourceProviders = new ArrayList<ResourceProvider>();
        for (Class<?> apiEndpoint: apiEndpoints){
			resourceProviders.add(new SingletonResourceProvider(apiEndpoint.newInstance()));
        }
        sf.setResourceProviders(resourceProviders);
        if (address != null && !address.isEmpty()){
        	sf.setAddress(address);
        }

        BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(sf.getBus());
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);

        sf.setInvoker(new JAXRSBeanValidationInvoker());
        Swagger2Feature swagger = new Swagger2Feature();
        swagger.setPrettyPrint(true);
        sf.getFeatures().add(swagger);
        sf.getFeatures().add(new JAXRSBeanValidationFeature());
        sf.setProviders(Lists.newArrayList(
        		new MultipartProvider(),
        		new JacksonJsonProvider(new HALMapper()),
        		new SearchContextProvider()));
        sf.create();
    }

    /**
     * Run the provided JAX-RS API endpoints on http://localhost:9000
     *
     * @param apiEndpoints is a list of the API endpoints that should served.
     */
    public static void run(Class<? extends ApiEndpointBase<? extends HalResource>>... apiEndpoints) {
    	run("http://localhost:9000", apiEndpoints);
    }

    /**
     * Run the provided JAX-RS API endpoints on the given address
     *
     * @param address is the URI where the endpoints should be served. If empty, the endpoints will be served on a random port on localhost
     * @param apiEndpoints is a list of the API endpoints that should served.
     */
	public static void run(String address, Class<? extends ApiEndpointBase<? extends HalResource>>... apiEndpoints){
	    try {
			new APIServer(address, apiEndpoints);
			LOGGER.info("Server ready...");
			Thread.sleep(5 * 6000 * 1000);
			LOGGER.info("Server exiting");
			System.exit(0);
		}
		catch (InstantiationException e) {
			LOGGER.error("An API endpoint could not be instantiated", e);
		}
		catch (IllegalAccessException e){
			LOGGER.error("An API endpoint could not be accessed", e);
		}
		catch (InterruptedException e){
			LOGGER.debug("The API server has been interrupted", e);
		}
	}
}
