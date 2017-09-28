/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathvector.impl.util;

import com.google.common.base.Optional;
import org.opendaylight.alto.pathvector.impl.util.helper.ReadDataFailedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.address.tracker.rev140617.address.node.connector.Addresses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.HostNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.host.tracker.rev140624.host.AttachmentPoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.alto.pathvector.impl.util.helper.DataStoreHelper;


//Just for test

import java.util.List;
import java.util.ArrayList;


public class Inventory {

    private Logger LOG = LoggerFactory.getLogger((Inventory.class));
    private DataBroker dataBroker;
    private static final String TOPO_ID = "flow:1";
    private static final String HOST = "host:";
    private static final InstanceIdentifier<Topology> TOPO_IID = InstanceIdentifier.create(NetworkTopology.class).child(Topology.class, new TopologyKey(new TopologyId("flow:1")));
    private static final String HOST_NODE_PREFIX = "host";
    private Topology topo = null;


    public Inventory(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        try {
            topo = DataStoreHelper.readFromDataStore(dataBroker,TOPO_IID,LogicalDatastoreType.OPERATIONAL);
        } catch (ReadDataFailedException e){
            e.printStackTrace();
        }
    }

    public MacAddress getMacByIp(Ipv4Address ipv4Address) {
        List<Node> nodes = topo.getNode();
        LOG.info(topo.toString());
        List<Addresses> addresses = null;
        for(Node node : nodes) {
            if(node.getNodeId().getValue().startsWith(HOST_NODE_PREFIX)) {
                HostNode hostNode = node.getAugmentation(HostNode.class);
                addresses = hostNode.getAddresses();
                if(addresses.size()==0) {
                    return null;
                }
                for(Addresses addrs : addresses) {
                    if(addrs.getIp().getIpv4Address().getValue().equals(ipv4Address.getValue())) {
                        return addrs.getMac();
                    }
                }
            }
        }
        return null;
    }
    public Link getSrcLinkByIp(Ipv4Address ipv4Address) {


        List<Node> nodes = topo.getNode();
        HostNode hostNode = null;
        List<Addresses> hostAddresses = null;

        for(Node node : nodes) {
            //Check if it a host node by node ID prefix
            LOG.info(node.toString());
            if(node.getNodeId().getValue().startsWith(HOST_NODE_PREFIX)) {
                hostNode = node.getAugmentation(HostNode.class);
                hostAddresses = hostNode.getAddresses();
                if(hostAddresses.size() == 0) {
                    return null;
                }
                for(Addresses addrs : hostAddresses) {
                    if(addrs.getIp().getIpv4Address().getValue().equals(ipv4Address.getValue())) {
                        TpId tpId = hostNode.getAttachmentPoints().get(0).getKey().getTpId();
                        Link link = getLinkByDst(tpId);
                        return link;
                    }
                }
            }
        }

        return null;
    }

    public Link getDstLinkByIp(Ipv4Address ipv4Address) {
        List<Node> nodes = topo.getNode();
        HostNode hostNode = null;
        List<Addresses> hostAddresses = null;

        for(Node node : nodes) {
            //Check if it a host node by node ID prefix
            LOG.info(node.toString());
            if(node.getNodeId().getValue().startsWith(HOST_NODE_PREFIX)) {
                hostNode = node.getAugmentation(HostNode.class);
                hostAddresses = hostNode.getAddresses();
                if(hostAddresses.size() == 0) {
                    return null;
                }
                for(Addresses addrs : hostAddresses) {
                    if(addrs.getIp().getIpv4Address().getValue().equals(ipv4Address.getValue())) {
                        TpId tpId = hostNode.getAttachmentPoints().get(0).getKey().getTpId();
                        Link link = getLinkBySrc(tpId);
                        return link;
                    }
                }
            }
        }

        return null;
    }
    public Link getLinkByDst(TpId tpId) {
        List<Link> links = topo.getLink();
        for(Link link : links) {
            if(link.getDestination().getDestTp().equals(tpId)) {
                return link;
            }
        }
        return null;
    }

    public Link getLinkBySrc(TpId tpId) {
        List<Link> links = topo.getLink();
        for(Link link : links) {
            if(link.getSource().getSourceTp().equals(tpId)) {
                return link;
            }
        }
        return null;
    }

    public boolean endWithHost(Link link) {
        LOG.info("HOST" + link.getDestination().getDestTp().getValue());
       if(link.getDestination().getDestNode().getValue().startsWith(HOST_NODE_PREFIX)) {
           return true;
       } else {
           return false;
       }
    }

    public boolean startWithHost(Link link) {
        LOG.info("HOST" + link.getSource().getSourceTp().getValue());
        if(link.getSource().getSourceTp().getValue().startsWith(HOST_NODE_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }

}
