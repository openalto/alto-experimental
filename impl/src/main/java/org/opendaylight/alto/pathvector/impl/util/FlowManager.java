/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathvector.impl.util;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.alto.pathvector.impl.util.helper.*;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.alto.pathvector.impl.util.helper.DataStoreHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class FlowManager{
    private static final Logger LOG = LoggerFactory.getLogger(FlowManager.class);

    private Map<Short,Table> tables = new HashMap<Short,Table>();
    private Map<Long, Group> groups = new HashMap<Long,Group>();
    private FlowEntryMatcher flowEntryMatcher = new FlowEntryMatcher();
    private List<Action> applyActions = new ArrayList<Action>();
    private FlowCapableNode flowCapableNode;
    private MatchFields matchFields;
    private TpId tpId;
    public boolean sendToController;
    private String nodeId;
    public DataBroker dataBroker;

    public FlowManager(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.matchFields = null;
        this.flowCapableNode = null;
    }

    public void setMatchFields(MatchFields matchFields) {
        LOG.info("MATCHFIELDS=" + matchFields.getSrcMac() + matchFields.getDstMac());
        this.matchFields = matchFields;
    }



    private FlowCapableNode getFlowCapableNode(String nodeId) {

        try {
            return DataStoreHelper.readOperational(dataBroker,
                    InstanceIdentifierHelper.flowCapableNode(nodeId));
        } catch (ReadDataFailedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void update(TpId tpId, NodeId nodeId) {
        LOG.info("UPDATE");
        this.nodeId = nodeId.getValue();
        this.matchFields.SetInPort(tpId,nodeId);
        this.flowCapableNode = getFlowCapableNode(nodeId.getValue());
    }

    public TpId getOutputTpId() {
        Prepare();
        Short index = 0;
        List<Flow> flows = tables.get(index).getFlow();
        sortFlowByPriority(flows);
        for(Flow flow : flows) {
            LOG.info("FLOW=:"+flow.toString());
            LOG.info("MATCH=" + flow.getMatch() + "MATCHFIELDS=" + matchFields.getSrcMac().getValue() + matchFields.getDstMac().getValue() + matchFields.getEthernetType().toString());
            if(flowEntryMatcher.match(flow.getMatch(), matchFields)) {
                Instructions inss = flow.getInstructions();
                if(inss!= null){
                    lookUpInstructions(inss.getInstruction());
                    LOG.info("GOTMATCHED " + flow.getMatch().toString());
                    Uri outputNodeConnector = processApplyAction(applyActions);
                    if(outputNodeConnector!=null)
                        return new TpId(nodeId+":"+outputNodeConnector.getValue());
                }

            }
        }
        return null;
    }



    protected void lookUpInstructions(List<Instruction> inss) {
        Map<Integer, Instruction> indexedInss = sortAndIndexInstructionsByOrder(inss);
        for (int i = 0; i < indexedInss.size(); i++) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction ins = indexedInss.get(i).getInstruction();
            if (ins instanceof ApplyActionsCase) {
                applyActionsCase((ApplyActionsCase) ins);

            }
        }
    }

    protected Uri processApplyAction(List<Action> actionList) {
        Action act = actionList.get(0);

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = act.getAction();
        if (action instanceof OutputActionCase) {
            return processOutputAction((OutputActionCase) action);
        }
        return null;
    }



    private Uri processOutputAction(OutputActionCase action) {
        Uri nodeConnector = action.getOutputAction().getOutputNodeConnector();
        return nodeConnector;

    }
    protected void Prepare() {
        tables.clear();
        groups.clear();
        applyActions.clear();
        tables = indexByTableId(flowCapableNode.getTable());
        groups = indexByGroupId(flowCapableNode.getGroup());
    }


    private void applyActionsCase(ApplyActionsCase applyActionCase) {
        List<Action> applyActions = applyActionCase.getApplyActions().getAction();
        sortActionsByOrder(applyActions);
        this.applyActions.addAll(applyActions);
    }


    private Map<Integer, Instruction> sortAndIndexInstructionsByOrder(List<Instruction> inns) {
        sortInstructionByOrder(inns);
        return indexInstructionsByOrder(inns);
    }
    private void sortInstructionByOrder(List<Instruction> inns) {
        Collections.sort(inns, new Comparator<Instruction>() {
            @Override
            public int compare(Instruction i1, Instruction i2) {
                if (i1.getOrder() < i2.getOrder()){
                    return -1;
                }else if (i1.getOrder() > i2.getOrder()){
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    private void sortActionsByOrder(List<Action> actions) {
        Collections.sort(actions, new Comparator<Action>() {
            @Override
            public int compare(Action a1, Action a2) {
                if (a1.getOrder() < a2.getOrder()){
                    return -1;
                }else if (a1.getOrder() > a2.getOrder()){
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    private Map<Integer, Instruction> indexInstructionsByOrder(List<Instruction> inns) {
        Map<Integer, Instruction> indexedInss = new HashMap<Integer, Instruction>();
        for (Instruction ins : inns) {
            int id = ins.getOrder();
            indexedInss.put(id, ins);
        }
        return indexedInss;
    }

    private Map<Short, Table> indexByTableId(List<Table> tables) {
        Map<Short, Table> indexedTables = new HashMap<Short, Table>();
        for (Table table : tables) {
            short id = table.getId();
            indexedTables.put(id, table);
        }
        return indexedTables;
    }

    private Map<Long, Group> indexByGroupId(List<Group> groups) {
        Map<Long, Group> groupMaps = new HashMap<Long, Group>();
        if (groups == null) {
            return groupMaps;
        }
        for (Group group: groups) {
            groupMaps.put(group.getGroupId().getValue(), group);
        }
        return groupMaps;
    }

    private void ClearAll() {
        tables.clear();
        groups.clear();
    }


    protected void sortFlowByPriority(List<Flow> flows) {
        Collections.sort(flows, new Comparator<Flow>() {
            @Override
            public int compare(Flow f1, Flow f2) {
                if (f1.getPriority() < f2.getPriority()){
                    return 1;
                } else if (f1.getPriority() > f2.getPriority()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

}
