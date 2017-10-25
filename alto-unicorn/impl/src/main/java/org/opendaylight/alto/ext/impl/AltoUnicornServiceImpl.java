/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.AltoUnicornService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.PathQueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.PathQueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.PathQueryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.ResourceQueryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.ResourceQueryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.ResourceQueryOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class AltoUnicornServiceImpl implements AltoUnicornService {

  private ResourceQueryImpl resourceQueryService;
  private PathQueryImpl pathQueryService;

  public AltoUnicornServiceImpl(DataBroker dataBroker) {
    resourceQueryService = new ResourceQueryImpl(dataBroker);
    pathQueryService = new PathQueryImpl(dataBroker);
  }

  @Override
  public Future<RpcResult<ResourceQueryOutput>> resourceQuery(ResourceQueryInput input) {
    ResourceQueryOutputBuilder output = new ResourceQueryOutputBuilder();

    return RpcResultBuilder.success(output
        .setAnes(resourceQueryService.computeResource(input.getResourceQueryDesc()))
        .build()).buildFuture();
  }

  @Override
  public Future<RpcResult<PathQueryOutput>> pathQuery(PathQueryInput input) {
    PathQueryOutputBuilder output = new PathQueryOutputBuilder();

    return RpcResultBuilder.success(output
        .setNextIngressPoint(pathQueryService.getNextIngressPoint(input.getPathQueryDesc()))
        .build()).buildFuture();
  }
}
