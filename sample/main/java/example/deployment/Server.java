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

package example.deployment;

import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.ext.search.SearchContextProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.provider.MultipartProvider;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationFeature;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInvoker;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Lists;

import dk.nykredit.jackson.dataformat.hal.HALMapper;
import example.resources.ApiEndpoint;

public class Server {
    protected Server() throws Exception {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(ApiEndpoint.class);
        sf.setResourceProvider(ApiEndpoint.class,
            new SingletonResourceProvider(new ApiEndpoint())); 	// http://localhost:9000/resources
        sf.setAddress("http://localhost:9000/");

        BindingFactoryManager manager = sf.getBus().getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(sf.getBus());
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);

        sf.setInvoker(new JAXRSBeanValidationInvoker());
        Swagger2Feature swagger = new Swagger2Feature(); 		// http://localhost:9000/api-docs?/url=/swagger.json
        swagger.setPrettyPrint(true);							// http://localhost:9000/swagger.json
        sf.getFeatures().add(swagger);
        sf.getFeatures().add(new JAXRSBeanValidationFeature());
        sf.setProviders(Lists.newArrayList(
        		new MultipartProvider(),
        		new JacksonJsonProvider(new HALMapper()),
        		new SearchContextProvider(),
        		new ApiOriginFilter()));
        sf.create();
    }

    public static void main(String args[]) throws Exception {
        new Server();
        System.out.println("Server ready...");

        Thread.sleep(5 * 6000 * 1000);
        System.out.println("Server exiting");
        System.exit(0);
}
}
