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
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.NodeKey;
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

    @Override
    public Future<RpcResult<BwmonitorRegisterOutput>> bwmonitorRegister(BwmonitorRegisterInput input) {
        LOG.debug("Get Input: " + input.getPortId());
        boolean success = true;
        if(!dataTreeInitialized) success = initializeDataTree();
        if(success) success = BwmonitorUtils.writeToSpeeds(input.getPortId(), (long)0, (long)0, db);
        BwmonitorRegisterOutput output = new BwmonitorRegisterOutputBuilder()
                .setResult(success).build();
        LOG.debug("Register node: " + input.getPortId());
        this.bwFetchingService.addListeningPort(input.getPortId());
        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<BwmonitorQueryOutput>> bwmonitorQuery(BwmonitorQueryInput input) {
        ReadTransaction transaction = db.newReadOnlyTransaction();
        InstanceIdentifier<Node> iid = InstanceIdentifier.create(Speeds.class).child(Node.class, new NodeKey(input.getPortId()));
        try {
            Optional<Node> nodeData = transaction.read(LogicalDatastoreType.OPERATIONAL, iid).get();
            if(nodeData.isPresent()){
                BwmonitorQueryOutput output = new BwmonitorQueryOutputBuilder()
                        .setRxSpeed(nodeData.get().getRxSpeed())
                        .setTxSpeed(nodeData.get().getTxSpeed())
                        .build();
                LOG.debug("Query node: " + input.getPortId() + ", RxSpeed: " + output.getRxSpeed() + ", TxSpeed: " + output.getTxSpeed());
                return RpcResultBuilder.success(output).buildFuture();
            }
        } catch (InterruptedException|ExecutionException e){
            LOG.error(e.getMessage());
        }
        BwmonitorQueryOutput output = new BwmonitorQueryOutputBuilder()
                .setRxSpeed(-1)
                .setTxSpeed(-1)
                .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    @Override
    public Future<RpcResult<BwmonitorDeregisterOutput>> bwmonitorDeregister(BwmonitorDeregisterInput input) {
        this.bwFetchingService.removeListeningPort(input.getPortId());
        WriteTransaction transaction = db.newWriteOnlyTransaction();
        InstanceIdentifier<Node> iid = InstanceIdentifier.create(Speeds.class).child(Node.class, new NodeKey(input.getPortId()));
        transaction.delete(LogicalDatastoreType.OPERATIONAL, iid);
        BwmonitorDeregisterOutput output = new BwmonitorDeregisterOutputBuilder().setResult(true).build();
        return RpcResultBuilder.success(output).buildFuture();
    }
}
