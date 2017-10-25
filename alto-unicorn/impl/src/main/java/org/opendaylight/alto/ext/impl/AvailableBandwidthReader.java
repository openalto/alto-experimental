/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.alto.ext.impl.helper.DataStoreHelper;
import org.opendaylight.alto.ext.impl.helper.ReadDataFailedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.Speeds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.Port;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvailableBandwidthReader {

  private final static Logger LOG = LoggerFactory.getLogger(AvailableBandwidthReader.class);

  private final DataBroker dataBroker;
  private Map<String, Long> bandwidthMap = new HashMap<>();

  public AvailableBandwidthReader(final DataBroker dataBroker) {
    this.dataBroker = dataBroker;
    syncBandwidthMonitor();
  }

  /**
   * Sync available bandwidth from alto-bwmonitor.
   */
  private void syncBandwidthMonitor() {
    try {
      Speeds speeds = DataStoreHelper.readOperational(dataBroker,
          InstanceIdentifier.create(Speeds.class));
      if (speeds != null && speeds.getPort() != null) {
        for (Port port : speeds.getPort()) {
          bandwidthMap.put(port.getPortId(), port.getAvailBw());
        }
      }
    } catch (ReadDataFailedException e) {
      LOG.error("Fail to sync data from bandwidth monitor:", e);
    }
  }

  /**
   * Lookup available bandwidth by port id.
   * @param portId the id of the queried port (the node connector id in opendaylight inventory)
   * @return the available bandwidth
   */
  public Long get(String portId) {
    return bandwidthMap.getOrDefault(portId, 0L);
  }
}
