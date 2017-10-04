/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathquery;

import java.util.HashMap;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/pathquery")
public class PathQuery {

    private static final Logger LOG = LoggerFactory.getLogger(PathQuery.class);

    HashMap<String, PeerInfo> peers;

    @GET
    public String test() {
        return "I get it!";
    }

    @POST
    public Response query(Query query) {
        for (FlowDesc fd: query.flows) {
            LOG.info("receiving flow: {} -> {} from {}", fd.src, fd.dst, fd.ingress);
        }
        return Response.status(200).build();
    }
}
