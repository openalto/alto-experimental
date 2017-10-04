/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathquery;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.LinkedListMultimap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Path("/pathquery")
public class PathQuery {

    private static final Logger LOG = LoggerFactory.getLogger(PathQuery.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(FlowDesc flow) {
        PathManager pm = PathManager.getInstance();

        LOG.debug("Processing flow: {} -> {} from {}",
                  flow.src, flow.dst, flow.ingress);

        try {
            List<NextHop> path = process(pm, flow);
            DomainPath entity = new DomainPath(path);
            return Response.status(200).entity(entity).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(e).build();
        }
    }

    private List<NextHop> process(PathManager pm, FlowDesc flow) throws Exception {
        String ingress = flow.ingress;
        String domain = pm.getCurrent();
        if (flow.ingress == null) {
            if (pm.contains(flow.src)) {
                ingress = pm.getPort(flow.src);
            } else {
                // We don't process flows that are not in the domain
                throw new RuntimeException("Failed: source " + flow.src
                                           + " not in domain {}" + domain);
            }
        }

        NextHop firstHop = new NextHop(domain, ingress);
        if (pm.contains(flow.dst)) {
            return ImmutableList.of(firstHop);
        }
        PeerInfo peer = pm.nextHop(flow);
        return ImmutableList.<NextHop>builder()
            .add(firstHop)
            .addAll(recurse(peer, flow))
            .build();
    }

    private List<NextHop> recurse(PeerInfo peer,
                                  FlowDesc flow) throws Exception {
        FlowDesc nextFlow = new FlowDesc(flow.src, flow.dst, peer.ingress);

        Client client = Client.create();

        WebResource resource = client.resource(peer.url);

        ClientResponse response = resource.type("application/json")
            .accept("application/json")
            .post(ClientResponse.class, nextFlow);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                                       + response.getStatus());
        }

        DomainPath path = response.getEntity(DomainPath.class);
        return path.path;
    }
}
