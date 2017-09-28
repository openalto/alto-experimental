/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathvector.impl;
import org.opendaylight.alto.pathvector.impl.util.Inventory;
import org.opendaylight.alto.pathvector.impl.util.FlowManager;
import org.opendaylight.alto.pathvector.impl.util.helper.DataStoreHelper;
import org.opendaylight.alto.pathvector.impl.util.helper.InstanceIdentifierHelper;
import org.opendaylight.alto.pathvector.impl.util.helper.MatchFields;
import org.opendaylight.alto.pathvector.impl.util.helper.ReadDataFailedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.PathvectorService;
import org.opendaylight.alto.pathvector.impl.util.service.NetworkFlowCapableNodeService;
import org.opendaylight.alto.pathvector.impl.util.NetworkFlowCapableNodeImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.QueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.QueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.endpoint.pair.ip.domain.IpV4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.entity.properties.EntityProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.property.map.domain.ane.*;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.property.map.domain.Ane;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.property.map.domain.AneBuilder;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.alto.pathvector.impl.util.GetHttp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.endpoint.pair.ip.domain.IpV4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.endpoint.flows.EndpointFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.endpoint.flows.EndpointFlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pathvector.rev150105.endpoint.flows.EndpointFlowBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.entity.properties.entity.property.Property;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.input.parameters.InputDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.entity.properties.entity.property.PropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.entity.properties.EntityPropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.property.map.domain.ane.PropertyMapEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.propertymap.rev150105.property.map.domain.ane.PropertyMapEntry;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.util.Iterator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PathVectorImpl implements PathvectorService{

    private DataBroker dataBroker;
    private static final Logger LOG = LoggerFactory.getLogger(PathVectorProvider.class);
    private static final String COST_MODE ="cost-mdoe";
    private static final String COST_METRIC = "cost-metric";
    private static final String PATH_VECTOR = "path-vector";
    private static final String ENDPOINT_FLOWS = "endpoint-flows";
    private static final String SRC = "src";
    private static final String DST = "dst";
    private static final String IPV4 = "ipv4:";
    private final static String QUOTATION = "\"";
    private final static int QUOTATION_SPLIT = 3;
    private final static String ANE = "ane";
    private static final String ROUTE = "route";
    private static final String BW = "bw";
    private static final String RESULT = "result";
    private static final String RESOURCE_ID = "resource_id";
    private static final String TAG = "tag";
    private static final String QUERY_ID = "query-id";
    private static final String ENDPOINT_COST_MAP = "endpoint-cost-map";

    public static final InstanceIdentifier<PropertyMapEntry> PROP_ENTRY = InstanceIdentifier.create(PropertyMap.class).child(PropertyMapEntry.class);

    private NetworkFlowCapableNodeService networkFlowCapableNodeService ;
    private FlowManager flowManager;
    private Inventory inventory ;

    public PathVectorImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        networkFlowCapableNodeService = new NetworkFlowCapableNodeImpl(dataBroker);
        flowManager =new FlowManager(dataBroker);
        LOG.info("PATHVECTORIMPL");
    }

    @Override
    public Future<RpcResult<QueryOutput>> query(QueryInput input) {
        inventory = new Inventory(dataBroker);

        //字符串的预处理
        int key = 0;
        int flowNumber = 0;
        List<EndpointFlow> endpointFlows = new ArrayList<EndpointFlow>();
        JsonNode _endpointFlows = null;
        Iterator<JsonNode> _endpointFlowsIterator = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode _filter = mapper.readTree(input.getName());
            _endpointFlows = _filter.get(ENDPOINT_FLOWS);
            _endpointFlowsIterator = _endpointFlows.elements();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (_endpointFlowsIterator != null) {
            while (_endpointFlowsIterator.hasNext()) {
                key++;
                JsonNode _endpointpair = _endpointFlowsIterator.next();
                JsonNode _src = _endpointpair.get(SRC);
                JsonNode _dst = _endpointpair.get(DST);
                if (_src != null && _dst != null) {
                    String splitSrc = _src.toString().split(QUOTATION,QUOTATION_SPLIT)[1].split(IPV4, 2)[1];
                    String splitDst = _dst.toString().split(QUOTATION,QUOTATION_SPLIT)[1].split(IPV4, 2)[1];;
                    Ipv4Address src = new Ipv4Address(splitSrc);
                    Ipv4Address dst = new Ipv4Address(splitDst);
                    IpV4 ipV4 = new IpV4Builder().setSrc(src).setDst(dst).build();
                    EndpointFlowKey endpointFlowKey = new EndpointFlowKey(key);
                    EndpointFlow endpointFlow = new EndpointFlowBuilder().setKey(endpointFlowKey).setIpDomain(ipV4).build();
                    endpointFlows.add(endpointFlow);
                    flowNumber ++;
                }
            }
        }



        //字符串的预处理结束


        Map<EndpointFlow,List<Link>> map = new HashMap<EndpointFlow,List<Link>>();
        Map<Link,List<Integer>> mapFlow = new HashMap<Link,List<Integer>>();
        Map<Link,Long> mapMetric = new HashMap<Link,Long>();
        List<Link> linkAll = new ArrayList<Link>();

        int identity = 0;
        for(Iterator<EndpointFlow> endpointFlowIterator = endpointFlows.iterator(); endpointFlowIterator.hasNext();) {
            EndpointFlow endpointFlowRef = endpointFlowIterator.next();
            identity = endpointFlowRef.getKey().getId();
            Ipv4Address ipSrc = ((IpV4)(endpointFlowRef.getIpDomain())).getSrc();
            Ipv4Address ipDst = ((IpV4)(endpointFlowRef.getIpDomain())).getDst();
            MacAddress macSrc = inventory.getMacByIp(ipSrc);
            MacAddress macDst = inventory.getMacByIp(ipDst);
            MatchFields matchFields = new MatchFields(macSrc,macDst);
            List<Link> links = new ArrayList<Link>();

            Link linkSrc = inventory.getSrcLinkByIp(ipSrc);
            Link linkDst = inventory.getDstLinkByIp(ipDst);

            if(linkSrc!=null && linkDst!=null && macSrc!=null && macDst!=null) {
                boolean send = false;
                Link link = linkSrc;
                links.add(link);
                map.put(endpointFlowRef,links);
                flowManager.setMatchFields(matchFields);
                updateMapMetric(mapMetric,link,send);
                send = true;
                while(inventory.endWithHost(link) == false) {
                    if(mapFlow.containsKey(link) == false) {
                        LOG.info("LINK=" + link.toString());
                        linkAll.add(link);
                    }
                    mapFlow = updateMapFlow(mapFlow,link,identity);
                    links.add(link);
                    flowManager.update(link.getDestination().getDestTp(),link.getDestination().getDestNode());
                    TpId dstTp = flowManager.getOutputTpId();
                    link = inventory.getLinkBySrc(dstTp);
                    map.put(endpointFlowRef,links);
                    updateMapMetric(mapMetric,link,send);
                }

                if(mapFlow.containsKey(link) == false) {
                    LOG.info("LINK="+link.toString());
                    linkAll.add(link);
                }

                mapFlow = updateMapFlow(mapFlow,link,identity);

            }
        }

        String str = null;
        /*str += "FLOW_INFORMATION";
        for(int i=1;i<=linkAll.size();i++) {
            List<Integer> temp = mapFlow.get(linkAll.get(i-1));
            str += "Link" + i + "=:";
            for(int j=1;j<= temp.size();j++)
                str += "Flow" + j + "=" + temp.get(j-1);
            str +=";";
        }
        str +="METRIC_INFORMATION";
        for(int i=1;i<=linkAll.size();i++) {
            str += "Link" + i + "=:";
            if(mapMetric.containsKey(linkAll.get(i-1)))
            {
                Long metric = mapMetric.get(linkAll.get(i-1));
                str += "Metric=" + metric;
            }
        }*/

        String queryId = String.valueOf(System.nanoTime());
        str = GetHttp.test(mapFlow,mapMetric,linkAll);
        Map<Integer,List<String>> aneFlow;
        aneFlow=processResponse(str,queryId);
        String pathVector;
        pathVector = buildOutput(endpointFlows,aneFlow,queryId);
        QueryOutputBuilder helloBuilder = new QueryOutputBuilder();
        helloBuilder.setGreeting(pathVector);
        return RpcResultBuilder.success(helloBuilder.build()).buildFuture();
    }


    private Map<Link,List<Integer>> updateMapFlow(Map<Link,List<Integer>> map, Link link, int identity) {
        List<Integer> integers =null;
        if (map.containsKey(link))
            integers = map.get(link);
        else
            integers = new ArrayList<Integer>();
        integers.add(new Integer(identity));
        map.put(link, integers);
        return map;
    }

    private Map<Link,Long> updateMapMetric(Map<Link,Long> map, Link link, boolean send) {
        if(map.containsKey(link) == false) {
            String flowCapableNodeConnector = null;
            if(inventory.startWithHost(link))
                flowCapableNodeConnector = link.getDestination().getDestTp().getValue();
            else
                flowCapableNodeConnector = link.getSource().getSourceTp().getValue();
            Long capacity = networkFlowCapableNodeService.getCapacity(flowCapableNodeConnector);
            Long available = networkFlowCapableNodeService.getAvailableBandwidth(flowCapableNodeConnector,send);
            Long consumed = networkFlowCapableNodeService.getConsumedBandwidth(flowCapableNodeConnector,send);
            map.put(link,available);
        }
        return map;
    }
    private FlowCapableNodeConnector getFlowCapableNodeConnector(String nodeId) {
        try {
            return DataStoreHelper.readOperational(dataBroker,
                    InstanceIdentifierHelper.flowCapableNodeConnector(nodeId));
        } catch (ReadDataFailedException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Map<Integer,List<String>> processResponse(String response, String id) {
        Map<Integer,List<String>> mapFlow = new HashMap<Integer,List<String>>();
        Map<String,Long> mapMetric = new HashMap<String, Long>();
        Iterator<JsonNode> _results = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode _root = mapper.readTree(response);
            JsonNode _result = _root.get(RESULT);
            _results = _result.elements();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int count = 0;
        List<String> anes = new ArrayList<String>();
        if(_results != null) {
            while(_results.hasNext()) {
                count ++;
                JsonNode _itresult = _results.next();
                JsonNode _route = _itresult.get(ROUTE);
                JsonNode _bw = _itresult.get(BW);
                String ane = ANE + count;
                Iterator<JsonNode> _flows = _route.elements();
                if(_flows != null) {
                    while(_flows.hasNext()) {
                        JsonNode _flow = _flows.next();
                        List<String> aneRef = null;
                        Integer integer = new Integer(_flow.toString());
                        if(mapFlow.containsKey(integer))
                             aneRef = mapFlow.get(integer);
                         else
                            aneRef = new ArrayList<String>();
                        aneRef.add(ane);
                        mapFlow.put(integer,aneRef);
                    }
                }

                Long metric = new Long(_bw.toString());
                mapMetric.put(ane,metric);
                anes.add(ane);
            }
        }

        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        List<EntityProperty> entityPropertyList = new ArrayList<EntityProperty>();
        List<PropertyMapEntry> propertyMapEntryList = new ArrayList<PropertyMapEntry>();
        for(Iterator<String> aneIterator = anes.iterator(); aneIterator.hasNext();) {
            String aneRef = aneIterator.next();
            List<Property> propertyList = new ArrayList<Property>();
            List<String> entities = new ArrayList<String>();
            PropertyName propertyName = new PropertyName("availbw");
            PropertyValue propertyValue= new PropertyValue(mapMetric.get(aneRef).toString());
            EntityName entityName = new EntityName(aneRef.toString());
            propertyList.add(new PropertyBuilder().setPropertyName(propertyName).setPropertyValue(propertyValue).build());
            entityPropertyList.add(new EntityPropertyBuilder().setEntityName(entityName).setProperty(propertyList).build());

        }

        QueryId queryId =new QueryId(id);
        PropertyMapEntryKey propertyMapEntryKey = new PropertyMapEntryKey(queryId);
        PropertyMapEntry propertyMapEntry = new PropertyMapEntryBuilder().setQueryId(queryId).setEntityProperty(entityPropertyList).build();
        InstanceIdentifier<PropertyMapEntry> propertyMapEntryPath = InstanceIdentifier.builder(PropertyMap.class).child(PropertyMapEntry.class,propertyMapEntryKey).build();

        tx.put(LogicalDatastoreType.OPERATIONAL,propertyMapEntryPath,propertyMapEntry,true);
        tx.submit();

        return mapFlow;
    }

    private String buildOutput(List<EndpointFlow> endpointFlowList,Map<Integer,List<String>> mapFlow, String queryId) {
        String response = "";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ArrayNode flowEntries = mapper.createArrayNode();
        for(Iterator<EndpointFlow> endpointFlowIterator = endpointFlowList.iterator(); endpointFlowIterator.hasNext();) {
            EndpointFlow endpointFlowRef = endpointFlowIterator.next();
            List<String> anes = mapFlow.get(endpointFlowRef.getId());
            ObjectNode pathVector = mapper.createObjectNode();
            ArrayNode _anes = mapper.createArrayNode();
            for(Iterator<String> aneIterator = anes.iterator();aneIterator.hasNext();) {
                String aneRef = aneIterator.next();
                _anes.add(aneRef);
            }
            pathVector.put(SRC,((IpV4)(endpointFlowRef.getIpDomain())).getSrc().getValue());
            pathVector.put(DST,((IpV4)(endpointFlowRef.getIpDomain())).getDst().getValue());
            pathVector.put(ANE,_anes);

            flowEntries.add(pathVector);
        }
        root.put(QUERY_ID,queryId);
        root.put(ENDPOINT_COST_MAP,flowEntries);
        try
        {
            response = mapper.writeValueAsString(root);
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }

        return response;
    }
}
