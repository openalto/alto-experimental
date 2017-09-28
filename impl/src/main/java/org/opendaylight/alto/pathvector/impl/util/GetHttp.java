/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.pathvector.impl.util;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetHttp {
    private static final String RSA = "http://192.168.1.25:8000/rsa";
    private static final String TYPE = "type";
    private static final String MECS = "mecs";
    private static final String ROUTE = "route";
    private static final String BW = "bw";
    private static final String CONSTRAINTS = "constraints";
    private static final String RESULT = "result";

    private static final Logger LOG = LoggerFactory
            .getLogger(GetHttp.class);

    public static String test (Map<Link,List<Integer>> mapFlow, Map<Link,Long> mapMetric, List<Link> links) {
        String data = null;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE,MECS);
        ArrayNode routes = mapper.createArrayNode();
        for(int i=0; i<links.size();i++) {
            ObjectNode node = mapper.createObjectNode();
            ArrayNode flows = mapper.createArrayNode();
            List<Integer> integers = mapFlow.get(links.get(i));
            for(int j = 0; j<integers.size();j++)
                flows.add(integers.get(j).longValue());
            node.put(ROUTE,flows);
            node.put(BW,mapMetric.get(links.get(i)));
            routes.add(node);
        }
        root.put(CONSTRAINTS,routes);
        try
        {
            data = mapper.writeValueAsString(root);
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }

        if(data!=null) {
            try {
                //Send Request
                URL url = new URL(RSA);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);conn.setDoOutput(true);
                //data = "{\"type\": \"random\", \"constraints\": [{\"route\": [1], \"bw\": 99.873}, {\"route\": [2, 3], \"bw\": 97.242}, {\"route\": [1, 2, 3], \"bw\": 102.12}]}";
                byte[] transfer = data.getBytes();
                conn.getOutputStream().write(transfer);
                LOG.info("Ready");
                // Get Response
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                        LOG.info("Reading");
                    }
                    in.close();
                    return response.toString();
                }
                return ""+responseCode;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }
}
