/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * PathListener Tester.
 *
 * @author Jensen Zhang
 * @version 1.0
 * @since <pre>Oct 10, 2017</pre>
 */
public class PathListenerTest {

  private static final Short DEFAULT_TABLE_ID = 0;
  private static final Uri DEFAULT_FLOW_ID = new Uri("flow:default");

  private final DataBroker dataBroker = mock(DataBroker.class);
  private PathListener pathListener;

  @Before
  public void before() throws Exception {
    pathListener = new PathListener(dataBroker);
  }

  @After
  public void after() throws Exception {
  }

  private DataTreeIdentifier<Flow> getMockRootPath(TableKey tableKey, FlowKey flowKey) {
    InstanceIdentifier<Flow> iid = InstanceIdentifier
        .builder(Nodes.class).child(Node.class)
        .augmentation(FlowCapableNode.class).child(Table.class, tableKey)
        .child(Flow.class, flowKey).build();
    DataTreeIdentifier<Flow> path = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, iid);

    return path;
  }

  private DataObjectModification<Flow> getMockRootNode(ModificationType type, Flow beforeData,
      Flow afterData) {
    DataObjectModification<Flow> node = mock(DataObjectModification.class);
    when(node.getModificationType()).thenReturn(type);
    when(node.getDataBefore()).thenReturn(beforeData);
    when(node.getDataAfter()).thenReturn(afterData);

    return node;
  }

  private String getRandomId(String prefix) {
    return prefix + ":" + UUID.randomUUID().toString().substring(0, 8);
  }

  private FlowBuilder getIpv4FlowBuilder(FlowId id) {
    return new FlowBuilder().setId(id)
        .setMatch(new MatchBuilder().setLayer3Match(
            new Ipv4MatchBuilder()
                .setIpv4Destination(new Ipv4Prefix("10.0.0.1/24"))
                .build())
            .build());
  }

  private Flow getIpv4DropFlow(FlowId id) {
    return getIpv4FlowBuilder(id).build();
  }

  private Flow getIpv4OutputFlow(FlowId id) {
    return getIpv4FlowBuilder(id)
        .setInstructions(new InstructionsBuilder()
            .setInstruction(Arrays.asList(new InstructionBuilder()
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(Arrays.asList(new ActionBuilder()
                            .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(getRandomId("openflow")))
                                    .build())
                                .build())
                            .build()))
                        .build())
                    .build())
                .build()))
            .build())
        .build();
  }

  /**
   * Method: onDataTreeChanged(@Nonnull Collection<DataTreeModification<Flow>> changes)
   */
  @Test
  public void testNullValueChanged() throws Exception {
    try {
      Collection<DataTreeModification<Flow>> nullChanges = null;
      pathListener.onDataTreeChanged(nullChanges);
    } catch (Exception e) {
      Assert.assertEquals(IllegalArgumentException.class, e.getClass());
    }

    Collection<DataTreeModification<Flow>> changes = new ArrayList<>();

    DataTreeModification<Flow> change0 = null;
    changes.add(change0);

    pathListener.onDataTreeChanged(changes);
  }

  /**
   * Method: onFlowRuleCreated(InstanceIdentifier<Flow> iid, Flow dataBefore, Flow dataAfter)
   */
  @Test
  public void testOnFlowRuleCreated() throws Exception {
    Collection<DataTreeModification<Flow>> changes = new ArrayList<>();

    DataTreeModification<Flow> change0 = mock(DataTreeModification.class);
    when(change0.getRootPath()).thenReturn(getMockRootPath(new TableKey(DEFAULT_TABLE_ID),
        new FlowKey(new FlowId(DEFAULT_FLOW_ID))));
    DataObjectModification<Flow> rootNode = getMockRootNode(ModificationType.WRITE, null,
        getIpv4DropFlow(new FlowId(DEFAULT_FLOW_ID)));
    when(change0.getRootNode()).thenReturn(rootNode);
    changes.add(change0);

    pathListener.onDataTreeChanged(changes);
  }

  /**
   * Method: onFlowRuleUpdated(InstanceIdentifier<Flow> iid, Flow dataBefore, Flow dataAfter)
   */
  @Test
  public void testOnFlowRuleUpdated() throws Exception {
    Collection<DataTreeModification<Flow>> changes = new ArrayList<>();

    DataTreeModification<Flow> change0 = mock(DataTreeModification.class);
    FlowId id0 = new FlowId(new Uri(getRandomId("flow")));
    when(change0.getRootPath()).thenReturn(getMockRootPath(new TableKey(DEFAULT_TABLE_ID),
        new FlowKey(id0)));
    DataObjectModification<Flow> rootNode0 =getMockRootNode(ModificationType.WRITE,
        getIpv4DropFlow(id0), getIpv4OutputFlow(id0));
    when(change0.getRootNode()).thenReturn(rootNode0);
    changes.add(change0);

    DataTreeModification<Flow> change1 = mock(DataTreeModification.class);
    FlowId id1 = new FlowId(new Uri(getRandomId("flow")));
    when(change1.getRootPath()).thenReturn(getMockRootPath(new TableKey(DEFAULT_TABLE_ID),
        new FlowKey(id1)));
    DataObjectModification<Flow> rootNode1 =getMockRootNode(ModificationType.SUBTREE_MODIFIED,
        getIpv4OutputFlow(id1), getIpv4DropFlow(id1));
    when(change1.getRootNode()).thenReturn(rootNode1);
    changes.add(change1);

    pathListener.onDataTreeChanged(changes);
  }

  /**
   * Method: onFlowRuleDeleted(InstanceIdentifier<Flow> iid, Flow dataBefore)
   */
  @Test
  public void testOnFlowRuleDeleted() throws Exception {
    Collection<DataTreeModification<Flow>> changes = new ArrayList<>();

    DataTreeModification<Flow> change0 = mock(DataTreeModification.class);
    FlowId id0 = new FlowId(new Uri(getRandomId("flow")));
    when(change0.getRootPath()).thenReturn(getMockRootPath(new TableKey(DEFAULT_TABLE_ID),
        new FlowKey(id0)));
    DataObjectModification<Flow> rootNode0 =getMockRootNode(ModificationType.DELETE,
        getIpv4OutputFlow(id0), null);
    when(change0.getRootNode()).thenReturn(rootNode0);
    changes.add(change0);

    pathListener.onDataTreeChanged(changes);
  }

}
