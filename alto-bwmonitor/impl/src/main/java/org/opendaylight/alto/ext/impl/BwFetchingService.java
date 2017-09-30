/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BwFetchingService implements DataTreeChangeListener<FlowCapableNodeConnectorStatisticsData>{
    /**
     * Global settings
     */
    private DataBroker dataBroker;
    private Logger LOG = LoggerFactory.getLogger(BwFetchingService.class);
    private ListenerRegistration<?> portListner = null;

    /**
     * Contructor to get essential variables
     * @param dataBroker Given by Provider, to help r/w data store
     */
    public BwFetchingService(DataBroker dataBroker){
        LOG.info("BwFetchingService initialized");
        rxMap = new HashMap<>();
        this.dataBroker = dataBroker;
        registerPortListener();
    }

    /**
     * Map to store the essential infomation
     * Map: (Port name -> (Timestamp -> Receive bytes))
     */
    private Map<String, Map<Double, Integer>> rxMap;

    private void syncToDataBroker(){

    }

    private void registerPortListener(){
        InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> iid = InstanceIdentifier
                .builder(Nodes.class)
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class)
                .child(NodeConnector.class).augmentation(FlowCapableNodeConnectorStatisticsData.class).build();
        this.portListner = this.dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, iid), this);
        LOG.info("BwFetchingService register successfully");
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<FlowCapableNodeConnectorStatisticsData>> changes) {

    }

    public void addListeningPort(String portId){
        if(!this.rxMap.containsKey(portId)){
            rxMap.put(portId, new HashMap<>());
            LOG.debug("Add listening port: " + portId);
        } else {
            LOG.debug("Try to add existent listening port: " + portId);
        }
    }

    public void removeListeningPort(String portId) {
        if(this.rxMap.containsKey(portId)){
            rxMap.remove(portId);
            LOG.debug("Remove listening port: " + portId);
        } else {
            LOG.debug("Try to remove nonexistent listening port: " + portId);
        }
    }
}
