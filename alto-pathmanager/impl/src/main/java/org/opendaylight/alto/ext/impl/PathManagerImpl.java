/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.AltoPathmanagerService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.HelloOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.pathmanager.rev150105.HelloOutputBuilder;
import java.util.concurrent.Future;

public class PathManagerImpl implements AltoPathmanagerService {

  @Override
  public Future<RpcResult<HelloOutput>> hello(HelloInput input) {
    HelloOutputBuilder helloOutputBuilder = new HelloOutputBuilder();
    helloOutputBuilder.setMatch("yes");
    return RpcResultBuilder.success(helloOutputBuilder.build()).buildFuture();
  }
}
