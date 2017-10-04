/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.pathquery;

import java.util.Set;
import javax.ws.rs.core.Application;
import com.google.common.collect.ImmutableSet;

public class PathQueryApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(PathQuery.class);
    }

}
