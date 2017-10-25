/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.AltoUnicornService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnicornProvider {

  private static final Logger LOG = LoggerFactory.getLogger(UnicornProvider.class);

  private final DataBroker dataBroker;
  private final RpcProviderRegistry rpcProviderRegistry;
  private RpcRegistration<AltoUnicornService> unicornRpcReg;

  public UnicornProvider(final DataBroker dataBroker,
      final RpcProviderRegistry rpcProviderRegistry) {
    this.dataBroker = dataBroker;
    this.rpcProviderRegistry = rpcProviderRegistry;
  }


  /**
   * Method called when the blueprint container is created.
   */
  public void init() {
    unicornRpcReg = rpcProviderRegistry
        .addRpcImplementation(AltoUnicornService.class,
            new AltoUnicornServiceImpl(dataBroker));
    LOG.info("UnicornProvider Session Initiated");
  }

  /**
   * Method called when the blueprint container is destroyed.
   */
  public void close() {
    if (unicornRpcReg != null) {
      unicornRpcReg.close();
    }
    LOG.info("UnicornProvider Closed");
  }
}
