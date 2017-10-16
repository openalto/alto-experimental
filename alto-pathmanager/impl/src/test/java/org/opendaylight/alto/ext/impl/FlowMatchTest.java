/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;
import org.opendaylight.alto.ext.helper.PathManagerHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.Protocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.path.FlowDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.path.FlowDescBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.junit.Assert;
import org.junit.Test;

public class FlowMatchTest {
    public static FlowDesc toAltoFlowDescTest(String srcIp, String dstIp,int srcPort, int dstPort, int protocol, String srcMac, String dstMac) {
        FlowDescBuilder builder = new FlowDescBuilder();
        Ipv4Prefix ipv4SrcPrefix = null;
        Ipv4Prefix ipv4DstPrefix =null;

        if(srcIp != null && srcIp != "") {
            ipv4SrcPrefix = new Ipv4Prefix(srcIp);
            builder.setSrcIp(ipv4SrcPrefix);
        }

        if(dstIp != null && dstIp != "") {
            ipv4DstPrefix = new Ipv4Prefix(dstIp);
            builder.setDstIp(ipv4DstPrefix);
        }
        if(srcMac != null && srcMac != "") {
            MacAddress srcMacAddress = new MacAddress(srcMac);
            builder.setSrcMac(srcMacAddress);
        }
        if(dstMac != null && dstMac != "") {
            MacAddress dstMacAddress = new MacAddress(dstMac);
            builder.setDstMac(dstMacAddress);
        }

        if(protocol >= 0 && protocol <= 2) {
            if(protocol == 0)
                builder.setProtocol(Protocol.Tcp);
            else if(protocol == 1)
                builder.setProtocol(Protocol.Udp);
            else if(protocol == 2)
                builder.setProtocol(Protocol.Sctp);
            if(srcPort >=0 && srcPort <= 65535)
                builder.setSrcPort(new PortNumber(srcPort));
            if(dstPort >=0 && dstPort <= 65535)
                builder.setDstPort(new PortNumber(dstPort));

        }

        return builder.build();
    }
    @Test
    public void Test1() {
        FlowDesc flow1_rule = toAltoFlowDescTest("192.168.1.0/24","10.0.2.0/15",
                20,30,0,
                "12:23:34:34:67:89","");
        FlowDesc flow1_test = toAltoFlowDescTest("192.168.1.230/32","10.0.2.0/16",
                20,30,0,
                "12:23:34:34:67:89",
                "23:56:67:23:33:22");
        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule,flow1_test),true);
    }

    @Test
    public void Test2() {
        FlowDesc flow1_rule = toAltoFlowDescTest("192.168.1.0/24","10.0.2.0/16",
                20,30,0,
                "12:23:34:34:67:89","");
        FlowDesc flow1_test = toAltoFlowDescTest("192.168.1.230/32","10.0.2.0/15",
                20,30,0,
                "12:23:34:34:67:89","");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule,flow1_test),false);
    }

    @Test
    public void Test3() {
        FlowDesc flow1_rule = toAltoFlowDescTest("192.168.1.0/24","10.3.4.1/16",
                20,30,-2,
                "11:11:11:11:11:11","");
        FlowDesc flow1_test = toAltoFlowDescTest("192.168.1.230/32","10.3.34.2/24",
                20,30,0,
                "11:11:11:11:11:11","");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule,flow1_test),true);
    }

    @Test
    public void Test4() {
        FlowDesc flow1_rule = toAltoFlowDescTest("20.2.2.2/24","10.3.4.1/16",
                20,30,-2,
                "11:11:11:11:11:11","");
        FlowDesc flow1_test = toAltoFlowDescTest("192.168.1.230/32","10.3.34.2/24",
                20,30,0,
                "12:23:34:34:67:89","");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule,flow1_test),false);
    }

    @Test
    public void Test5() {
        FlowDesc flow1_rule = toAltoFlowDescTest("192.168.1.22/24","10.3.4.1/16",
                40,30,0,
                "11:11:11:11:11:11","");
        FlowDesc flow1_test = toAltoFlowDescTest("192.168.1.230/32","10.3.34.2/24",
                20,30,0,
                "12:23:34:34:67:89","");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule,flow1_test),false);
    }

    @Test
    public void Test6() {
        FlowDesc flow1_rule = toAltoFlowDescTest("192.168.1.22/24","10.3.4.1/16",
                20,30,-2,
                "23:34:55:34:33:33","");
        FlowDesc flow1_test = toAltoFlowDescTest("192.168.1.230/32","10.3.34.2/24",
                20,30,0,
                "11:11:11:11:11:11","");

        Assert.assertEquals(PathManagerHelper.isFlowMatch(flow1_rule,flow1_test),false);
    }


}
