/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathquery;

public class FlowDesc {
    public String src;

    public String dst;

    public String ingress;

    public FlowDesc() {
    }

    public FlowDesc(String src, String dst, String ingress) {
        this.src = src;
        this.dst = dst;
        this.ingress = ingress;
    }
}
