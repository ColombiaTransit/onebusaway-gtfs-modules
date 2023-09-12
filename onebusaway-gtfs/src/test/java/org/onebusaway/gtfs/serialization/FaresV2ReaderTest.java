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
package org.onebusaway.gtfs.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareLegRule;
import org.onebusaway.gtfs.model.FareMedium;
import org.onebusaway.gtfs.model.FareProduct;
import org.onebusaway.gtfs.model.FareTransferRule;
import org.onebusaway.gtfs.model.RiderCategory;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopAreaElement;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

public class FaresV2ReaderTest extends BaseGtfsTest {

  @Test
  public void turlockFaresV2() throws CsvEntityIOException, IOException {
    String agencyId = "1642";
    GtfsRelationalDao dao = processFeed(GtfsTestData.getTurlockFaresV2(),
      agencyId, false);

    Agency agency = dao.getAgencyForId(agencyId);
    assertEquals(agencyId, agency.getId());
    assertEquals("Turlock Transit", agency.getName());
    assertEquals("http://www.turlocktransit.com/", agency.getUrl());
    assertEquals("America/Los_Angeles", agency.getTimezone());

    List<FareProduct> fareProducts = new ArrayList<>(dao.getAllFareProducts());
    assertEquals(12, fareProducts.size());

    FareProduct fp = fareProducts.stream().sorted(Comparator.comparing(FareProduct::getId)).findFirst().get();
    assertEquals("id=31-day_disabled|category=disabled|medium=null", fp.getId().getId());
    assertEquals("31-Day Pass Persons with Disabilities", fp.getName());
    assertEquals("USD", fp.getCurrency());
    assertEquals(15.0, fp.getAmount(), 0);
    assertEquals(3, fp.getDurationUnit());
    assertEquals(31, fp.getDurationAmount());
    assertEquals(2, fp.getDurationType());
    RiderCategory cat = fp.getRiderCategory();
    assertEquals("Persons with Disabilities", cat.getName());
    assertEquals("disabled", cat.getId().getId());


    List<FareLegRule> fareLegRules = new ArrayList<>(dao.getAllFareLegRules());
    assertEquals(12, fareLegRules.size());

    FareLegRule flr = fareLegRules.stream().sorted(Comparator.comparing(FareLegRule::getId)).findFirst().get();
    assertEquals("groupId=Turlock|product=31-day_disabled|network=null|fromArea=null|toArea=null", flr.getId());
    assertEquals("Turlock", flr.getLegGroupId().getId());

    List<RiderCategory> riderCats = new ArrayList<>(dao.getAllRiderCategories());
    assertEquals(5, riderCats.size());

    RiderCategory riderCat = riderCats.stream().sorted(Comparator.comparing(RiderCategory::getId)).filter(c -> c.getId().getId().equals("youth")).findAny().get();
    assertEquals("youth", riderCat.getId().getId());
    assertEquals("Youth Age 18 and Under", riderCat.getName());
    assertEquals(18, riderCat.getMaxAge());
    assertEquals(RiderCategory.MISSING_VALUE, riderCat.getMinAge());
    assertEquals("http://www.turlocktransit.com/fares.html", riderCat.getEligibilityUrl());

    assertTrue(dao.hasFaresV1());
    assertTrue(dao.hasFaresV2());
  }
  @Test
  public void mdotMetroFaresV2() throws CsvEntityIOException, IOException {
    String agencyId = "1";
    GtfsRelationalDao dao = processFeed(GtfsTestData.getMdotMetroFaresV2(),
      agencyId, false);

    Agency agency = dao.getAgencyForId(agencyId);
    assertEquals(agencyId, agency.getId());
    assertEquals("Maryland Transit Administration Metro Subway", agency.getName());

    List<FareProduct> fareProducts = new ArrayList<>(dao.getAllFareProducts());
    assertEquals(21, fareProducts.size());

    FareProduct fp = fareProducts.stream().sorted(Comparator.comparing(FareProduct::getId)).findFirst().get();
    assertEquals("id=core_local_1_day_fare|category=null|medium=charmcard", fp.getId().getId());
    assertEquals("1-Day Pass - Core Service", fp.getName());
    assertEquals("USD", fp.getCurrency());
    assertEquals(4.6, fp.getAmount(), 0.01);

    List<FareLegRule> fareLegRules = new ArrayList<>(dao.getAllFareLegRules());
    assertEquals(7, fareLegRules.size());

    FareLegRule flr = fareLegRules.stream().sorted(Comparator.comparing(FareLegRule::getId)).findFirst().get();
    assertEquals("groupId=core_local_one_way_trip|product=core_local_1_day_fare|network=core|fromArea=null|toArea=null", flr.getId());
    assertEquals("core_local_one_way_trip", flr.getLegGroupId().getId());

    List<FareTransferRule> fareTransferRules = new ArrayList<>(dao.getAllFareTransferRules());
    assertEquals(3, fareTransferRules.size());

    FareTransferRule ftr = fareTransferRules.stream().sorted(Comparator.comparing(FareTransferRule::getId)).findFirst().get();
    assertEquals("1_core_express_one_way_trip_1_core_express_one_way_trip_null_-999_5400", ftr.getId());
    assertEquals(new AgencyAndId("1", "core_express_one_way_trip"), ftr.getFromLegGroupId());
    assertEquals(-999, ftr.getTransferCount());
    assertEquals(5400, ftr.getDurationLimit());

    List<FareMedium> media = new ArrayList<>(dao.getAllFareMedia());
    assertEquals(3, fareTransferRules.size());

    FareMedium medium = media.stream().filter(c -> c.getId().getId().equals("charmcard_senior")).findFirst().get();
    assertEquals("charmcard_senior", medium.getId().getId());
    assertEquals("Senior CharmCard", medium.getName());

    List<StopAreaElement> stopAreaElements = new ArrayList<>(dao.getAllStopAreaElements());
    assertEquals(0, stopAreaElements.size());

    List<Route> routes = new ArrayList<>(dao.getAllRoutes());
    assertEquals(1, routes.size());
    assertEquals("core", routes.get(0).getNetworkId());

    assertFalse(dao.hasFaresV1());
    assertTrue(dao.hasFaresV2());
  }


  @Test
  public void testFaresV2Distance() throws IOException{
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putMinimal();
    gtfs.putLines("fare_products.txt", "fare_product_id, amount, currency", "" +
            "fare_1,5,EUR");
    gtfs.putLines("fare_leg_rules.txt", "network_id,min_distance,max_distance,distance_type,fare_product_id",
            "bus,0,3,1,fare_1"
    );
    GtfsRelationalDao dao = processFeed(gtfs.getPath(), "1", false);
    assertTrue(dao.getAllFareLegRules().stream().map(fareLegRule -> fareLegRule.getMaxDistance()).findFirst().get() == 3.0);
    assertTrue(dao.getAllFareLegRules().stream().map(fareLegRule -> fareLegRule.getMinDistance()).findFirst().get() == 0.0);
    assertTrue(dao.getAllFareLegRules().stream().map(fareLegRule -> fareLegRule.getDistanceType()).findFirst().get() == 1);
  }

}
