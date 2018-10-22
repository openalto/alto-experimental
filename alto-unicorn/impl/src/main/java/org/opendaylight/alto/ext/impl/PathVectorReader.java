/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.alto.basic.helper.PathManagerHelper;
import org.opendaylight.alto.basic.impl.helper.DataStoreHelper;
import org.opendaylight.alto.basic.impl.helper.ReadDataFailedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.PathManager;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.Path;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.path.FlowDesc;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.path.FlowDescBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.pathmanager.rev150105.path.manager.path.Links;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.alto.query.desc.Flow;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathVectorReader {

  private final static Logger LOG = LoggerFactory.getLogger(PathVectorReader.class);
  private final static TopologyId DEFAULT_TOPOLOGY_ID = new TopologyId("flow:1");
  private final static InstanceIdentifier<Topology> DEFAULT_TOPOLOGY_IID = InstanceIdentifier
      .create(NetworkTopology.class)
      .child(Topology.class, new TopologyKey(DEFAULT_TOPOLOGY_ID));

  private final DataBroker dataBroker;
  private List<Path> pathList = new ArrayList<>();
  private Map<LinkId, String> linkMap = new HashMap<>();

  public PathVectorReader(final DataBroker dataBroker) {
    this.dataBroker = dataBroker;
    syncPathManager();
    syncNetworkTopology();
  }

  /**
   * Sync path vector recorder from alto-pathmanager.
   */
  private void syncPathManager() {
    try {
      PathManager pathManager = DataStoreHelper.readOperational(dataBroker, InstanceIdentifier
          .create(PathManager.class));
      if (pathManager != null && pathManager.getPath() != null) {
        pathList = pathManager.getPath();
        pathList.sort(Comparator.comparing(Path::getId).reversed());
      }
    } catch (ReadDataFailedException e) {
      LOG.error("Fail to sync data from path manager:", e);
    }
  }

  /**
   * Sync topology data from network-topology.
   */
  private void syncNetworkTopology() {
    try {
      Topology topology = DataStoreHelper.readOperational(dataBroker, DEFAULT_TOPOLOGY_IID);
      if (topology != null && topology.getLink() != null) {
        for (Link link : topology.getLink()) {
          linkMap.put(link.getLinkId(), link.getSource().getSourceTp().getValue());
        }
      }
    } catch (ReadDataFailedException e) {
      LOG.error("Fail to sync data from network topology model:", e);
    }
  }

  /**
   * Convert Flow object of query input into FlowDesc object.
   * @param flow flow object of query input
   * @return the equivalent FlowDesc object.
   */
  private FlowDesc toAltoFlowDesc(Flow flow) {
    if (flow == null) {
      return null;
    }
    return new FlowDescBuilder()
        .setSrcMac(flow.getSrcMac())
        .setDstMac(flow.getDstMac())
        .setSrcIp(flow.getSrcIp())
        .setDstIp(flow.getDstIp())
        .setProtocol(flow.getProtocol())
        .setSrcPort(flow.getSrcPort())
        .setDstPort(flow.getDstPort())
        .build();
  }

  /**
   * Lookup path-manager from DataStore to get path vector of a flow.
   * @param flow flow object of query input
   * @return path vector (a list of egress port)
   */
  public List<String> get(Flow flow) {
    List<String> egressPorts = new ArrayList<>();

    for (Path path : pathList) {
      if (PathManagerHelper.isFlowMatch(path.getFlowDesc(), toAltoFlowDesc(flow))) {
        for (Links links : path.getLinks()) {
          String egressPortId = linkMap.get(links.getLink());
          if (egressPortId != null) {
            egressPorts.add(egressPortId);
          }
        }
        break;
      }
    }

    return egressPorts;
  }
}
