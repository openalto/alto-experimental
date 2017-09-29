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
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

public class BwFetchingService implements DataTreeChangeListener<FlowCapableNodeConnectorStatisticsData>{
    private DataBroker dataBroker;
    private Logger LOG = LoggerFactory.getLogger(BwFetchingService.class);

    public BwFetchingService(DataBroker dataBroker){
        LOG.info("BwFetchingService initialized");
        this.dataBroker = dataBroker;
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<FlowCapableNodeConnectorStatisticsData>> changes) {

    }
}
