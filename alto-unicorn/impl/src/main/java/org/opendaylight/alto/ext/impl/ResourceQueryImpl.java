/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.resource.query.input.ResourceQueryDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.resource.query.output.Anes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.resource.query.output.AnesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.resource.query.output.anes.AneFlowCoefficientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceQueryImpl {

  private final static Logger LOG = LoggerFactory.getLogger(ResourceQueryImpl.class);

  private final DataBroker dataBroker;

  public ResourceQueryImpl(DataBroker dataBroker) {
    this.dataBroker = dataBroker;
  }

  /**
   * Compute raw resource state by looking up path-manager and bwmonitor.
   * @param queryDescs
   * @return a list of path vector
   */
  public List<Anes> computeResource(List<ResourceQueryDesc> queryDescs) {
    if (queryDescs == null) {
      return null;
    }

    Map<String, Anes> anesMap = new HashMap<>();
    AvailableBandwidthReader availBwReader = new AvailableBandwidthReader(dataBroker);
    PathVectorReader pvReader = new PathVectorReader(dataBroker);

    for (ResourceQueryDesc queryDesc : queryDescs) {
      for (String egressPort : pvReader.get(queryDesc.getFlow())) {
        if (! anesMap.containsKey(egressPort)) {
          anesMap.put(egressPort, new AnesBuilder()
              .setAneFlowCoefficient(new ArrayList<>())
              .setAvailbw(availBwReader.get(egressPort))
              .build());
        }
        anesMap.get(egressPort).getAneFlowCoefficient().add(
            new AneFlowCoefficientBuilder()
                .setFlowId(queryDesc.getFlowId())
                .build());
      }
    }

    List<Anes> anesList = new ArrayList<>();
    for (Anes anes : anesMap.values()) {
      anesList.add(anes);
    }
    return anesList;
  }

}
