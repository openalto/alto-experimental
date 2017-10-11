/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.helper;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.Protocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.path.FlowDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.path.FlowDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathManagerHelper {

  private static final Logger LOG = LoggerFactory.getLogger(PathManagerHelper.class);

  public static FlowDesc toAltoFlowDesc(Match match) {
    FlowDescBuilder builder = new FlowDescBuilder();
    if (match != null) {
      if (match.getLayer3Match() != null) {
        if (match.getLayer3Match() instanceof Ipv4Match) {
          Ipv4Match ipv4Match = (Ipv4Match) match.getLayer3Match();
          builder.setSrcIp(ipv4Match.getIpv4Source())
              .setDstIp(ipv4Match.getIpv4Destination());
          LOG.debug("toAltoFlowDesc: extract Ipv4Match.");
        } else if (match.getLayer3Match() instanceof Ipv6Match) {
          // Do not support ipv6 match now
          LOG.debug("toAltoFlowDesc: Currently we do not support IPv6Match in AltoFlowDesc~");
        }
      }

      if (match.getLayer4Match() != null) {
        if (match.getLayer4Match() instanceof TcpMatch) {
          TcpMatch tcpMatch = (TcpMatch) match.getLayer4Match();
          builder.setSrcPort(tcpMatch.getTcpSourcePort())
              .setDstPort(tcpMatch.getTcpDestinationPort())
              .setProtocol(Protocol.Tcp);
          LOG.debug("toAltoFlowDesc: extract TcpMatch.");
        } else if (match.getLayer4Match() instanceof UdpMatch) {
          UdpMatch udpMatch = (UdpMatch) match.getLayer4Match();
          builder.setSrcPort(udpMatch.getUdpSourcePort())
              .setDstPort(udpMatch.getUdpDestinationPort())
              .setProtocol(Protocol.Udp);
          LOG.debug("toAltoFlowDesc: extract UdpMatch.");
        } else if (match.getLayer4Match() instanceof SctpMatch) {
          SctpMatch sctpMatch = (SctpMatch) match.getLayer4Match();
          builder.setSrcPort(sctpMatch.getSctpSourcePort())
              .setDstPort(sctpMatch.getSctpDestinationPort())
              .setProtocol(Protocol.Sctp);
          LOG.debug("toAltoFlowDesc: extract SctpMatch.");
        }
      }
    }

    return builder.build();
  }

  public static List<Uri> toOutputNodeConnector(Instructions instructions) {
    List<Uri> egressPorts = new ArrayList<>();
    if (instructions != null) {
      for (Instruction instruction : instructions.getInstruction()) {
        if (instruction.getInstruction() instanceof ApplyActionsCase) {
          for (Action action : ((ApplyActionsCase) instruction.getInstruction())
              .getApplyActions().getAction()) {
            if (action.getAction() instanceof OutputActionCase) {
              egressPorts.add(((OutputActionCase) action.getAction())
                  .getOutputAction().getOutputNodeConnector());
              LOG.debug("toOutputNodeConnector: find OutputAction: {}.", action);
            }
          }
        }
      }
    }
    return egressPorts;
  }

  public static boolean isNullableObjectDiff(Object before, Object after) {
    if (before == null && after == null) {
      return false;
    }
    return true;
  }

  public static boolean isObjectDiff(Object before, Object after) {
    if (before != null && after != null) {
      return !before.equals(after);
    }
    return isNullableObjectDiff(before, after);
  }

  public static boolean isFlowRuleDiff(Flow before, Flow after) {
    if (before != null && after != null) {
      return isObjectDiff(toAltoFlowDesc(before.getMatch()), toAltoFlowDesc(after.getMatch())) ||
          isObjectDiff(toOutputNodeConnector(before.getInstructions()),
              toOutputNodeConnector(after.getInstructions()));
    }
    return isNullableObjectDiff(before, after);
  }
}
