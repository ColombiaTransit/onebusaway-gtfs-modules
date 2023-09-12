/**
 * Copyright (C) 2023 Leonard Ehrenfried <mail@leonard.io>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.model;

import java.util.HashSet;
import java.util.Set;

public class StopArea extends IdentityBean<AgencyAndId> implements StopLocation {

  private static final long serialVersionUID = 1L;

  private Area area;

  private Set<StopLocation> stops = new HashSet<>();

  @Override
  public AgencyAndId getId() {
    return area.getId();
  }

  @Override
  public void setId(AgencyAndId id) {
  }

  public void setArea(Area area) {
    this.area = area;
  }

  public Set<StopLocation> getLocations() {
    return stops;
  }

  private void setLocations(Set<StopLocation> stops) {
    this.stops = stops;
  }

  public void addLocation(StopLocation location) {
    this.stops.add(location);
  }

  public String getName() {
    return area.getName();
  }

  @Override
  public void setName(String name) {

  }

}
