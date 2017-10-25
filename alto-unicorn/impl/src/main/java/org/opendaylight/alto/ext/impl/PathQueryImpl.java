/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.ext.impl;

import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.path.query.input.PathQueryDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.ext.unicorn.rev150105.path.query.output.NextIngressPoint;

public class PathQueryImpl {

  private final DataBroker dataBroker;

  public PathQueryImpl(DataBroker dataBroker) {
    this.dataBroker = dataBroker;
  }

  public List<NextIngressPoint> getNextIngressPoint(List<PathQueryDesc> queryDescs) {
    return null;
  }
}
