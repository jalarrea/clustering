/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;


/**
 * @author stefan schroeder
 */

public interface RuinStrategy {

    /**
     * Ruins a current solution, i.e. a collection of vehicle-routes and
     * returns a collection of removed and thus unassigned jobs.
     *
     * @param {@link VehicleRoute}
     * @return Collection of {@link com.graphhopper.jsprit.core.problem.job.Job}
     */
    public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes);

    /**
     * Adds a ruin-listener.
     *
     * @param {@link RuinListener}
     */
    public void addListener(RuinListener ruinListener);

    public void removeListener(RuinListener ruinListener);

    public Collection<RuinListener> getListeners();

}
