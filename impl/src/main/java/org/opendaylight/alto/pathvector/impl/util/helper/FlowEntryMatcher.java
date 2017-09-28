/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathvector.impl.util.helper;

import org.opendaylight.alto.pathvector.impl.PathVectorProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowEntryMatcher {


    private static final Logger LOG = LoggerFactory.getLogger(FlowEntryMatcher.class);

    /**
     * @param match is the original fields should be matched.
     * @param matchFields are the target fields should be compared.
     * @return the result of match.
     */
    public boolean match(Match match, MatchFields matchFields) {
        return (match == null) ||
                ((matchInPort(match.getInPort(), matchFields))
                        && matchEthernet(match.getEthernetMatch(), matchFields));


    }




    private boolean matchInPort(NodeConnectorId port, MatchFields matchFields) {
        if (port == null) return true;
        String flowInPort = port.getValue();
        return flowInPort.equals(matchFields.getInPort());
    }




    public boolean matchEthernet(EthernetMatch match, MatchFields matchFields) {
        return (match == null)
                || (matchEthernetSrc(match.getEthernetSource(), matchFields)
                && matchEthernetDst(match.getEthernetDestination(), matchFields)
                && matchEthernetType(match.getEthernetType(), matchFields));
    }

    private boolean matchEthernetSrc(EthernetSource ethSrc, MatchFields matchFields) {
        return (ethSrc == null) ||
                matchMacAddressWithMask(ethSrc.getAddress(), matchFields.getSrcMac(), ethSrc.getMask());
    }

    private boolean matchEthernetDst(EthernetDestination ethDest, MatchFields matchFields) {
        return (ethDest == null) ||
                matchMacAddressWithMask(ethDest.getAddress(), matchFields.getDstMac(), ethDest.getMask());
    }

    public boolean matchMacAddressWithMask(MacAddress macA, MacAddress macB, MacAddress mask) {
        LOG.info("MAC1=" + normalizeMacAddress(macA) + "MAC2=" + normalizeMacAddress(macB));
        LOG.info("MAC1==MAC2?" +  normalizeMacAddress(macA).equals(normalizeMacAddress(macB) ) );
        if(mask!=null)
            LOG.info("MASKMAC1=" + getMaskedMacAddress(normalizeMacAddress(macA), normalizeMacAddress(mask)) +
                    "MASKMAC2" + getMaskedMacAddress(normalizeMacAddress(macB), normalizeMacAddress(mask)));
        return (mask == null && normalizeMacAddress(macA).equals(normalizeMacAddress(macB)))
                || (mask != null && getMaskedMacAddress(normalizeMacAddress(macA), normalizeMacAddress(mask))
                == getMaskedMacAddress(normalizeMacAddress(macB), normalizeMacAddress(mask)));
    }

    private long getMaskedMacAddress(String macAddress, String mask) {
        long macLong = (Long.parseLong(macAddress,16));
        long maskLong = Long.parseLong(mask,16);
        return macLong & maskLong;
    }

    private boolean matchEthernetType(EthernetType ethernetType, MatchFields matchFields) {
        return (ethernetType == null) || (matchFields.getEthernetType() != null
                && ethernetType.getType().getValue().longValue()
                == matchFields.getEthernetType().longValue());
    }
    private String normalizeMacAddress(MacAddress mac) {
        return mac.getValue().replaceAll(":|-", "").toLowerCase();
    }
}
