/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.pathvector.impl.util.helper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchFields {

    private MacAddress srcMac;
    private MacAddress dstMac;
    private String inPort;
    private Long ethernetType;
    private static final Logger LOG = LoggerFactory.getLogger(MatchFields.class);

    public MatchFields(MacAddress src, MacAddress dst) {

        this.srcMac = src;
        this.dstMac = dst;
        this.ethernetType = (long) 0x0800;
    }

    public void SetInPort(TpId tpId, NodeId nodeId) {
        LOG.info("INPORT=" + tpId.getValue().replace(nodeId.getValue()+":",""));
        this.inPort = tpId.getValue().replace(nodeId.getValue()+":","");
    }

    public MacAddress getSrcMac() {
        return this.srcMac;
    }
    public MacAddress getDstMac() {
        return this.dstMac;
    }
    public Long getEthernetType() {
        return this.ethernetType;
    }
    public String getInPort() {
        return  this.inPort;
    }

}
