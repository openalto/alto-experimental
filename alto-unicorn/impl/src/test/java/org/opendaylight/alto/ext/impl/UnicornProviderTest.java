/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

public class UnicornProviderTest {

  private final DataBroker dataBroker = mock(DataBroker.class);
  private final RpcProviderRegistry rpcProviderRegistry = mock(RpcProviderRegistry.class);
  private UnicornProvider provider;

  @Before
  public void setupProvider() throws Exception {
    provider = new UnicornProvider(dataBroker, rpcProviderRegistry);
  }

  @Test
  public void testInit() throws Exception {
    provider.init();
  }

  @Test
  public void testClose() throws Exception {
    provider.close();
  }

}
