/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.alto.ext.helper.PathManagerHelper;
import org.opendaylight.alto.ext.impl.help.DataStoreHelper;
import org.opendaylight.alto.ext.impl.help.ReadDataFailedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.PathManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.PathManagerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.path.manager.Path;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.path.manager.path.FlowDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathManagerUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(PathManagerUpdater.class);
  private static final Short DEFAULT_TABLE_ID = 0;
  private static final InstanceIdentifier<PathManager> PATH_MANAGER_IID = InstanceIdentifier
      .create(PathManager.class);
  private static final InstanceIdentifier<Nodes> INV_NODE_IID = InstanceIdentifier
      .create(Nodes.class);

  private final DataBroker dataBroker;

  public PathManagerUpdater(DataBroker dataBroker) {
    this.dataBroker = dataBroker;
    initiate();
  }

  private void initiate() {
    LOG.debug("Initializing path manager updater from FRM.");
    Nodes nodes = null;
    try {
      nodes = DataStoreHelper.readOperational(dataBroker, INV_NODE_IID);
    } catch (ReadDataFailedException e) {
      LOG.error("Read inv:node failed: ", e);
    }
    if (nodes != null && nodes.getNode() != null) {
      for (Node node : nodes.getNode()) {
        String nodeId = node.getId().getValue();
        FlowCapableNode flowNode = node.getAugmentation(FlowCapableNode.class);
        if (flowNode != null && flowNode.getTable() != null) {
          Table defaultTable = flowNode.getTable().get(DEFAULT_TABLE_ID);
          if (defaultTable != null && defaultTable.getFlow() != null) {
            for (Flow flow : defaultTable.getFlow()) {
              newFlowRule(nodeId, flow);
            }
          }
        }
      }
    }
    LOG.debug("Initialized path manager updater from FRM.");
  }

  public void newFlowRule(String nodeId, Flow flow) {
    LOG.debug("Flow rule of node {} created:\n{}.", nodeId, flow);
    FlowDesc flowDesc = PathManagerHelper.toAltoFlowDesc(flow.getMatch());
    List<Uri> egressPorts = PathManagerHelper.toOutputNodeConnector(flow.getInstructions());
    if (egressPorts == null || egressPorts.isEmpty()) {
      LOG.debug("No Egress Ports of this flow. Skip updating.");
      return;
    }
    PathManager pathManager = null;
    try {
      pathManager = DataStoreHelper.readOperational(dataBroker, PATH_MANAGER_IID);
    } catch (ReadDataFailedException e) {
      LOG.error("Read pathmanager:path-manager failed: ", e);
    }
    if (pathManager == null) {
      pathManager = new PathManagerBuilder().setPath(new ArrayList<>()).build();
    }
    List<Path> paths = pathManager.getPath();
    paths.sort((a, b) -> b.getId().compareTo(a.getId()));
    for (Path path : paths) {
      LOG.debug("Compare flowDesc of path and inserted flow: {} and {}.", flowDesc,
          path.getFlowDesc());
      List<FlowDesc> flowDescSet = getUnionFlowDesc(flowDesc, path.getFlowDesc());
      if (flowDescSet != null) {
        LOG.debug("FlowDesc does not match this path.");
      }
    }
  }

  private List<FlowDesc> getUnionFlowDesc(FlowDesc flowDesc, FlowDesc flowDesc1) {
    return null;
  }

  public void updateFlowRule(String nodeId, Flow before, Flow after) {
    LOG.debug("Flow rule of node {} updated:\nFrom: {};\nTo: {}.", nodeId, before, after);
  }

  public void deleteFlowRule(String nodeId, Flow flow) {
    LOG.debug("Flow rule of node {} deleted:\n{}.", nodeId, flow);
  }
}
