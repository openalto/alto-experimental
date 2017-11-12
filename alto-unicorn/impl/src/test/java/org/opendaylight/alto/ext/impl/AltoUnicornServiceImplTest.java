/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

import java.math.BigInteger;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.PathManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.PathManagerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.path.manager.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.path.manager.path.FlowDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.path.manager.path.LinksBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.AltoUnicornService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.PathQueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.PathQueryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.ResourceQueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.ResourceQueryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.alto.query.desc.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.resource.query.input.ResourceQueryDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.Speeds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.SpeedsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.alto.bwmonitor.rev150105.speeds.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.SourceBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * AltoUnicornServiceImpl Tester.
 *
 * @author Jensen Zhang
 * @version 1.0
 * @since <pre>Oct 17, 2017</pre>
 */
public class AltoUnicornServiceImplTest {

  private final static InstanceIdentifier<Speeds> SPEEDS_IID = InstanceIdentifier
      .create(Speeds.class);
  private final static InstanceIdentifier<PathManager> PATH_MANAGER_IID = InstanceIdentifier
      .create(PathManager.class);
  private final static TopologyId TOPOLOGY_ID = new TopologyId("flow:1");
  private final static InstanceIdentifier<Topology> TOPOLOGY_IID = InstanceIdentifier
      .create(NetworkTopology.class)
      .child(Topology.class, new TopologyKey(TOPOLOGY_ID));

  private final DataBroker dataBroker = mock(DataBroker.class);
  private AltoUnicornService unicornService;
  private ResourceQueryInput input1;

  @Before
  public void before() throws Exception {
    ReadOnlyTransaction rx = mock(ReadOnlyTransaction.class);

    CheckedFuture speedsFuture = mock(CheckedFuture.class);
    Optional speedsOptional = mock(Optional.class);
    Speeds speeds = new SpeedsBuilder()
        .setPort(Arrays.asList(
            new PortBuilder().setPortId("testPort1")
                .setRxSpeed(BigInteger.valueOf(0L))
                .setTxSpeed(BigInteger.valueOf(0L))
                .setCapacity(1000L)
                .setAvailBw(1000L).build(),
            new PortBuilder().setPortId("testPort2")
                .setRxSpeed(BigInteger.valueOf(0L))
                .setTxSpeed(BigInteger.valueOf(0L))
                .setCapacity(2000L)
                .setAvailBw(2000L).build()
        ))
        .build();
    when(speedsOptional.get()).thenReturn(speeds);
    when(speedsOptional.isPresent()).thenReturn(true);
    when(speedsFuture.get()).thenReturn(speedsOptional);
    when(rx.read(any(), eq(SPEEDS_IID))).thenReturn(speedsFuture);

    CheckedFuture pathFuture = mock(CheckedFuture.class);
    Optional pathOptional = mock(Optional.class);
    PathManager pathManager = new PathManagerBuilder()
        .setPath(Arrays.asList(new PathBuilder()
            .setId(0L)
            .setFlowDesc(new FlowDescBuilder()
                .setDstIp(new Ipv4Prefix("10.0.1.0/24"))
                .build())
            .setLinks(Arrays.asList(new LinksBuilder()
                .setLink(new LinkId("testLink0"))
                .build()))
            .build()))
        .build();
    when(pathOptional.get()).thenReturn(pathManager);
    when(pathOptional.isPresent()).thenReturn(true);
    when(pathFuture.get()).thenReturn(pathOptional);
    when(rx.read(any(), eq(PATH_MANAGER_IID))).thenReturn(pathFuture);

    CheckedFuture topoFuture = mock(CheckedFuture.class);
    Optional topoOptional = mock(Optional.class);
    Topology topology = new TopologyBuilder()
        .setLink(Arrays.asList(
            new LinkBuilder()
                .setLinkId(new LinkId("testLink0"))
                .setSource(new SourceBuilder()
                    .setSourceTp(new TpId("testPort1"))
                    .build())
                .build()))
        .build();
    when(topoOptional.get()).thenReturn(topology);
    when(topoOptional.isPresent()).thenReturn(true);
    when(topoFuture.get()).thenReturn(topoOptional);
    when(rx.read(any(), eq(TOPOLOGY_IID))).thenReturn(topoFuture);

    when(dataBroker.newReadOnlyTransaction()).thenReturn(rx);

    unicornService = new AltoUnicornServiceImpl(dataBroker);
    input1 = new ResourceQueryInputBuilder()
        .setResourceQueryDesc(Arrays.asList(
            new ResourceQueryDescBuilder()
                .setFlowId(0L)
                .setFlow(new FlowBuilder()
                    .setDstIp(new Ipv4Prefix("10.0.1.1/32"))
                    .build())
                .setIngressPoint(null)
                .build()))
        .build();
  }

  /**
   * Method: resourceQuery(ResourceQueryInput input)
   */
  @Test
  public void testResourceQuery() throws Exception {
    try {
      unicornService.resourceQuery(input1);
    } catch (Exception e) {
      Assert.fail();
    }
  }

  /**
   * Method: pathQuery(PathQueryInput input)
   */
  @Test
  public void testPathQuery() throws Exception {
    PathQueryInput input = new PathQueryInputBuilder().build();
    try {
      unicornService.pathQuery(input);
    } catch (Exception e) {
      Assert.fail();
    }
  }

}
