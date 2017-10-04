/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathquery;

public class PeerInfo {
    public String ingress;

    public String id;

    public String url;

    public PeerInfo(String id, String url, String ingress) {
        this.id = id;
        this.url = url;
        this.ingress = ingress;
    }
}
