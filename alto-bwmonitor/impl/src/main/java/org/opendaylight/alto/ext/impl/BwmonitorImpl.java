/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.ext.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BwmonitorImpl implements AltoBwmonitorService{
    private DataBroker db;
    private BwFetchingService bwFetchingService;
    private static final Logger LOG = LoggerFactory.getLogger(BwmonitorImpl.class);
    private boolean dataTreeInitialized = false;

    public BwmonitorImpl(DataBroker dataBroker, BwFetchingService bwFetchingService){
        this.db = dataBroker;
        this.bwFetchingService = bwFetchingService;
    }

    private boolean initializeDataTree(){
        WriteTransaction transaction = db.newWriteOnlyTransaction();
        InstanceIdentifier<Speeds> iid = InstanceIdentifier.create(Speeds.class);
        Speeds speeds = new SpeedsBuilder().build();
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, speeds);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<Void>("Failed to create speeds", LOG));

        try {
            future.checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error(e.getMessage());
            return false;
        }
        dataTreeInitialized = true;
        return true;
    }

    private boolean writeToSpeeds(BwmonitorRegisterInput input){
        WriteTransaction transaction = db.newWriteOnlyTransaction();
        InstanceIdentifier<Node> iid = toInstanceIdentifier(input);
        Node node = new NodeBuilder().setNodeId(input.getNodeId()).setSpeed(0).build();
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, node);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<Void>("Failed to write node to speeds", LOG));

        try {
            future.checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error(e.getMessage());
            return false;
        }
        return true;
    }

    private InstanceIdentifier<Node> toInstanceIdentifier(BwmonitorRegisterInput input){
        InstanceIdentifier<Node> iid = InstanceIdentifier.create(Speeds.class)
                .child(Node.class, new NodeKey(input.getNodeId()));
        return iid;
    }

    @Override
    public Future<RpcResult<BwmonitorRegisterOutput>> bwmonitorRegister(BwmonitorRegisterInput input) {
        LOG.error("Get Input: " + input.getNodeId());
        boolean success = true;
        if(!dataTreeInitialized) success = initializeDataTree();
        if(success) success = writeToSpeeds(input);
        BwmonitorRegisterOutput output = new BwmonitorRegisterOutputBuilder()
                .setResult(success).build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<BwmonitorQueryOutput>> bwmonitorQuery(BwmonitorQueryInput input) {
        ReadTransaction transaction = db.newReadOnlyTransaction();
        InstanceIdentifier<Node> iid = InstanceIdentifier.create(Speeds.class).child(Node.class, new NodeKey(input.getNodeId()));
        try {
            Optional<Node> nodeData = transaction.read(LogicalDatastoreType.OPERATIONAL, iid).get();
            if(nodeData.isPresent()){
                BwmonitorQueryOutput output = new BwmonitorQueryOutputBuilder()
                        .setBandwidth(nodeData.get().getSpeed()).build();
                return RpcResultBuilder.success(output).buildFuture();
            }
        } catch (InterruptedException|ExecutionException e){
            LOG.error(e.getMessage());
        }
        BwmonitorQueryOutput output = new BwmonitorQueryOutputBuilder()
                .setBandwidth(-1).build();
        return RpcResultBuilder.success(output).buildFuture();
    }
}
