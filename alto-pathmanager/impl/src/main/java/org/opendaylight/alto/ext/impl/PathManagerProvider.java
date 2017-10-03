/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.pathmanager.rev150105.AltoPathmanagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.alto.ext.impl.PathManagerImpl;

import org.opendaylight.alto.ext.impl.help.Header;
import org.opendaylight.alto.ext.impl.help.MatchFields;
public class PathManagerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PathManagerProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private RpcRegistration<AltoPathmanagerService> altoPathmanagerServiceRpcRegistration;

    public PathManagerProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("PathManagerProvider Session Initiated");
        altoPathmanagerServiceRpcRegistration = rpcProviderRegistry.addRpcImplementation(AltoPathmanagerService.class, new PathManagerImpl());
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("PathManagerProvider Closed");
    }
}