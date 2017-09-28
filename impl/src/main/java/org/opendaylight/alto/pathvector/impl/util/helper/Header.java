/*
 * Copyright © 2017 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathvector.impl.util.helper;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

public class Header {
    private Ipv4Address srcIp;
    private Ipv4Address dstIp;

    public Header(Ipv4Address srcIp, Ipv4Address dstIp) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
    }
}
