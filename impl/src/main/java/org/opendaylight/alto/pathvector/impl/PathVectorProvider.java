/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.pathvector.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.PathvectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.alto.pathvector.impl.util.Inventory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

public class PathVectorProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PathVectorProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;

    private RpcRegistration<PathvectorService> pathvectorServiceRpcRegistration;

    public PathVectorProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("PathVectorProvider Session Initiated");
        pathvectorServiceRpcRegistration = rpcProviderRegistry.addRpcImplementation(PathvectorService.class, new PathVectorImpl(dataBroker));
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        pathvectorServiceRpcRegistration.close();
        LOG.info("PathVectorProvider Closed");
    }

    public void test() {


    }


}