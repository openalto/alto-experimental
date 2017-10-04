/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathquery.test;

import java.util.Map;

import org.opendaylight.alto.pathquery.FlowDesc;
import org.opendaylight.alto.pathquery.PathManager;
import org.opendaylight.alto.pathquery.PeerInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class StaticPathManager extends PathManager {

    private static final Logger LOG = LoggerFactory.getLogger(StaticPathManager.class);

    Map<String, String> hostTable = ImmutableMap.of("10.0.0.1", "AS0:port1",
                                                    "10.0.0.2", "AS0:port2");

    PeerInfo peer1 = new PeerInfo("AS1", "http://localhost:6666/pathquery/", "1");
    PeerInfo peer2 = new PeerInfo("AS2", "http://localhost:9999/pathquery/", "2");

    Map<String, PeerInfo> peers = ImmutableMap.of("10.0.1.2", peer1,
                                                  "10.0.2.2", peer2);

    public void init() {
        if (PathManager.setInstance(this)) {
            LOG.info("Initializaing: setting the path manager instance");
        }
    }

    public void close() {
    }

    @Override
    public String getCurrent() {
        return "AS0";
    }

    @Override
    public boolean contains(String addr) {
        return hostTable.containsKey(addr);
    }

    @Override
    public String getPort(String addr) {
        return hostTable.get(addr);
    }

    @Override
    public PeerInfo nextHop(FlowDesc flow) {
        return peers.get(flow.dst);
    }
}
