/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathquery;

public abstract class PathManager {

    private static PathManager singleton = null;

    protected static boolean setInstance(PathManager pm) {
        if (singleton != null) {
            return false;
        }
        singleton = pm;
        return true;
    }

    public static PathManager getInstance() {
        return singleton;
    }

    /**
     * Return the current domain ID
     **/
    public abstract String getCurrent();

    /**
     * Whether a host with the `addr` ipv4 address is in the domain
     **/
    public abstract boolean contains(String addr);

    public abstract String getPort(String addr);

    /**
     * Return the information on next hop for the given request
     **/
    public abstract PeerInfo nextHop(FlowDesc fd);
}
