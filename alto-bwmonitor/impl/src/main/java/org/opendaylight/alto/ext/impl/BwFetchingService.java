/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.Bytes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.Speeds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BwFetchingService implements DataTreeChangeListener<FlowCapableNodeConnectorStatisticsData>{
    /**
     * Global settings
     */
    private DataBroker dataBroker;
    private Logger LOG = LoggerFactory.getLogger(BwFetchingService.class);
    private ListenerRegistration<?> portListner = null;
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    private final ExecutorService exec = Executors.newFixedThreadPool(CPUS);

    /**
     * Parameter to calculate the speed
     */
    private final Integer timeSpan = 3;

    /**
     * Contructor to get essential variables
     * @param dataBroker Given by Provider, to help r/w data store
     */
    public BwFetchingService(DataBroker dataBroker){
        LOG.info("BwFetchingService initialized");
        this.dataBroker = dataBroker;
        registerPortListener();
    }

    class Statistic{
        /**
         * Timestamp -> Bytes
         */
        Map<Long, Long> rxHistory;
        Map<Long, Long> txHistory;

        Long rxSpeed;
        Long txSpeed;

        public Statistic(){
            rxHistory = new HashMap<>();
            txHistory = new HashMap<>();
            rxSpeed = Long.valueOf(-1);
            txSpeed = Long.valueOf(-1);
        }
    }

    Map<String, Statistic> statisticData = new HashMap<>();

    private void syncToDataBroker(String name, Statistic statistic){
        BwmonitorUtils.writeToSpeeds(name, statistic.rxSpeed, statistic.txSpeed, dataBroker);
        LOG.info("Bwmonitor speeds updated: rxSpeed=" + statistic.rxSpeed + ", txSpeed=" + statistic.txSpeed);
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
        LOG.debug("Get Data Changed");
        exec.submit(()->{
            for(DataTreeModification<FlowCapableNodeConnectorStatisticsData> change: changes){
                DataObjectModification<FlowCapableNodeConnectorStatisticsData> rootNode = change.getRootNode();
                InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> iid = change.getRootPath().getRootIdentifier();
                switch (rootNode.getModificationType()){
                    case WRITE:
                    case SUBTREE_MODIFIED:
                        onFlowCapableNodeConnectorStatisticsDataUpdated(iid, rootNode.getDataAfter());
                        break;
                    case DELETE:
                        onFlowCapableConnectorStatisticsDataDeleted(iid);
                }
            }
        });
    }

    private void onFlowCapableConnectorStatisticsDataDeleted(InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> identifier) {
        String name = identifier.firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId().getValue();
        this.removeListeningPort(name);
    }

    private void onFlowCapableNodeConnectorStatisticsDataUpdated(InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> identifier, FlowCapableNodeConnectorStatisticsData originStatistic) {
        Bytes bytes = originStatistic.getFlowCapableNodeConnectorStatistics().getBytes();
        if (bytes != null) {
            Statistic statistic;
            String id = identifier
                    .firstKeyOf(NodeConnector.class, NodeConnectorKey.class)
                    .getId().getValue();
            if (statisticData.containsKey(id)) {
                statistic = statisticData.get(id);
            } else {
                statistic = new Statistic();
                statisticData.put(id, statistic);
            }
            Long timestamp = originStatistic.getFlowCapableNodeConnectorStatistics()
                    .getDuration().getSecond().getValue();
            statistic.rxHistory.put(timestamp, bytes.getReceived().longValue());
            statistic.txHistory.put(timestamp, bytes.getTransmitted().longValue());
            statistic.rxSpeed = computeStatisticFromHistory(statistic.rxHistory, timestamp);
            statistic.txSpeed = computeStatisticFromHistory(statistic.txHistory, timestamp);
            syncToDataBroker(id, statistic);
        }
    }

    private void cleanStatisticHistory(Map<Long, Long> history, Long timestamp, boolean inTimeSpan) {
        if(inTimeSpan){
            history.entrySet().removeIf(e -> e.getKey() < (timestamp - timeSpan));
        } else {
            Long maxTime = Long.valueOf(0);
            for(Map.Entry<Long, Long> item : history.entrySet()){
                if(item.getKey() != timestamp && item.getKey() > maxTime)
                    maxTime = item.getKey();
            }
            for(Iterator<Map.Entry<Long, Long>> it = history.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, Long> entry = it.next();
                if(entry.getKey() < maxTime) {
                    it.remove();
                }
            }
        }
    }

    private Long computeStatisticFromHistory(Map<Long, Long> history, Long timestamp) {
        /**
         * current speed = (average speed in timeSpan) * 0.8 + (speed from last history record) * 0.2
         */
        boolean inTimeSpan = false;
        for(Map.Entry<Long, Long> item : history.entrySet()){
            if(!item.getKey().equals(timestamp) && item.getKey() > timestamp - timeSpan){
                inTimeSpan = true;
                break;
            }
        }
        cleanStatisticHistory(history, timestamp, inTimeSpan);
        Long minTime = Long.MAX_VALUE;
        Long maxTime = (long)0;
        for(Map.Entry<Long, Long> item : history.entrySet()){
            if(item.getKey() != timestamp && item.getKey() < minTime)
                minTime = item.getKey();
            if(item.getKey() != timestamp && item.getKey() > maxTime)
                maxTime = item.getKey();
        }
        Long speedFromLastRecord = (history.get(maxTime) - history.get(timestamp)) / (timestamp - maxTime);
        if(inTimeSpan){
            Long speedFromTimeSpan = (history.get(minTime) - history.get(timestamp)) / (timestamp - minTime);
            return (long)(speedFromTimeSpan * 0.8 + speedFromLastRecord * 0.2);
        } else {
            return speedFromLastRecord;
        }
    }

    public void addListeningPort(String portId){
        if(!this.statisticData.containsKey(portId)){
            statisticData.put(portId, new Statistic());
            LOG.info("Add listening port: " + portId);
        } else {
            LOG.debug("Try to add existent listening port: " + portId);
        }
    }

    public void removeListeningPort(String portId) {
        if(this.statisticData.containsKey(portId)){
            statisticData.remove(portId);
            LOG.info("Remove listening port: " + portId);
        } else {
            LOG.debug("Try to remove nonexistent listening port: " + portId);
        }
    }
}
