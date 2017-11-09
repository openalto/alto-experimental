/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.Speeds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.PortKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BwmonitorUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BwmonitorUtils.class);

    private BwmonitorUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static InstanceIdentifier<Port> toInstanceIdentifier(String nodeId) {
        InstanceIdentifier<Port> iid = InstanceIdentifier.create(Speeds.class)
                .child(Port.class, new PortKey(nodeId));
        return iid;
    }

    public static boolean writeToSpeeds(String portId, Long rxSpeed, Long txSpeed,
            Long capacity, Long availBw, DataBroker db) {
        WriteTransaction transaction = db.newWriteOnlyTransaction();
        InstanceIdentifier<Port> iid = BwmonitorUtils.toInstanceIdentifier(portId);
        Port node = new PortBuilder().setPortId(portId)
                .setRxSpeed(BigInteger.valueOf(rxSpeed))
                .setTxSpeed(BigInteger.valueOf(txSpeed))
                .setCapacity(capacity)
                .setAvailBw(availBw)
                .build();
        transaction.put(LogicalDatastoreType.OPERATIONAL, iid, node);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        Futures.addCallback(future, new LoggingFuturesCallBack<>("Failed to write node to speeds", LOG));

        try {
            future.checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error(e.getMessage());
            return false;
        }
        return true;
    }
}

