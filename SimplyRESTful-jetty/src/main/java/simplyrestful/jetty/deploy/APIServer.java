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
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.ext.search.SearchContextProvider;
import org.apache.cxf.jaxrs.lifecycle.PerRequestResourceProvider;
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

/**
 * This represents the API server that runs the SimplyRESTful API.
 *
 * This will create a pre-configured Jetty instance that will run any JAX-RS Web Resource you provide it with. This is
 * intended as a quick way to get your SimplyRESTful API running and uses manual configuration to deploy Apache CXF.
 *
 * @author RiaasM
 *
 */
public class APIServer {
	private static final Logger LOGGER = LoggerFactory.getLogger("simplyrestful.jetty.deploy.APIServer");
	private Server cxfServer;

    /**
     * Create the API server with the provided JAX-RS API Web Resources on the given address.
     *
     * The Web Resource will have its lifecycle set to singleton. If this is not possible, e.g. if no
     * instance can be created for the Web Resource, it will fall back to a per-request lifecycle.
     *
     * @param address is the URI where the Web Resource should be served. If empty, the Web Resource will be served on a random port on localhost
     * @param webResources is a list of the JAX-RS Web Resources that should served.
     */
    public APIServer(String address, Class<?>... webResources){
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(webResources);
        ArrayList<ResourceProvider> resourceProviders = new ArrayList<ResourceProvider>();
        for (Class<?> webResource: webResources){
			try {
				resourceProviders.add(new SingletonResourceProvider(webResource.newInstance()));
			}
			catch (InstantiationException | IllegalAccessException e) {
				LOGGER.warn("Couldn't create an instance of this JAX-RS Web Resource for singleton lifecyce: " + webResource.getName());
				LOGGER.warn("Using per-request lifecycle for this JAX-RS Web Resource instead");
				resourceProviders.add(new PerRequestResourceProvider(webResource));
			}
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
        cxfServer = sf.create();
        LOGGER.info("Server ready...");
    }

    /**
     * Create a CXF-based API server with the provided JAX-RS Web Resources on http://localhost:9000
     *
     * @param webResources is a list of the JAX-RS Web Resources that should served.
     */
    public APIServer(Class<?>... webResources) {
    	this("http://localhost:9000", webResources);
    }

    /**
     * Retrieve the CXF server that was created for this API server.
     *
     * @return the CXF-based server created for this API server.
     */
    public Server getCXFServer(){
    	return cxfServer;
    }

}
