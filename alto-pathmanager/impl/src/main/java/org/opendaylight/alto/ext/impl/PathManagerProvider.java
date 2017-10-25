/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathManagerProvider {

  private static final Logger LOG = LoggerFactory.getLogger(PathManagerProvider.class);

  private final DataBroker dataBroker;
  private ListenerRegistration<PathListener> pathListenerReg;

  public PathManagerProvider(final DataBroker dataBroker) {
    this.dataBroker = dataBroker;
  }

  private void setListener() {
    this.pathListenerReg = dataBroker
        .registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL,
            getWildCardPath()), new PathListener(dataBroker));
  }

  private InstanceIdentifier<Flow> getWildCardPath() {
    return InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(FlowCapableNode
        .class).child(Table.class).child(Flow.class);
  }

  /**
   * Method called when the blueprint container is created.
   */
  public void init() {
    setListener();
    LOG.info("PathManagerProvider Session Initiated");
  }

  /**
   * Method called when the blueprint container is destroyed.
   */
  public void close() {
    if (pathListenerReg != null) {
      pathListenerReg.close();
    }
    LOG.info("PathManagerProvider Closed");
  }
}