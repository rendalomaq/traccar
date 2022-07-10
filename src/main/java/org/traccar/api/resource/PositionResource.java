/*
 * Copyright 2015 - 2022 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.api.resource;

import org.traccar.api.BaseResource;
import org.traccar.helper.model.PositionUtil;
import org.traccar.model.Device;
import org.traccar.model.Position;
import org.traccar.model.UserRestrictions;
import org.traccar.reports.CsvExportProvider;
import org.traccar.reports.KmlExportProvider;
import org.traccar.storage.StorageException;
import org.traccar.storage.query.Columns;
import org.traccar.storage.query.Condition;
import org.traccar.storage.query.Request;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Path("positions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PositionResource extends BaseResource {

    @Inject
    private KmlExportProvider kmlExportProvider;

    @Inject
    private CsvExportProvider csvExportProvider;

    @GET
    public Collection<Position> getJson(
            @QueryParam("deviceId") long deviceId, @QueryParam("id") List<Long> positionIds,
            @QueryParam("from") Date from, @QueryParam("to") Date to)
            throws StorageException {
        if (!positionIds.isEmpty()) {
            var positions = new ArrayList<Position>();
            for (long positionId : positionIds) {
                Position position = storage.getObject(Position.class, new Request(
                        new Columns.All(), new Condition.Equals("id", "id", positionId)));
                permissionsService.checkPermission(Device.class, getUserId(), position.getDeviceId());
                positions.add(position);
            }
            return positions;
        } else if (deviceId > 0) {
            permissionsService.checkPermission(Device.class, getUserId(), deviceId);
            if (from != null && to != null) {
                permissionsService.checkRestriction(getUserId(), UserRestrictions::getDisableReports);
                return PositionUtil.getPositions(storage, deviceId, from, to);
            } else {
                return storage.getObjects(Position.class, new Request(
                        new Columns.All(), new Condition.LatestPositions(deviceId)));
            }
        } else {
            return PositionUtil.getLatestPositions(storage, getUserId());
        }
    }

    @Path("kml")
    @GET
    @Produces("application/vnd.google-earth.kml+xml")
    public Response getKml(
            @QueryParam("deviceId") long deviceId,
            @QueryParam("from") Date from, @QueryParam("to") Date to) throws StorageException {
        permissionsService.checkPermission(Device.class, getUserId(), deviceId);
        StreamingOutput stream = output -> {
            try {
                kmlExportProvider.generate(output, deviceId, from, to);
            } catch (StorageException e) {
                throw new WebApplicationException(e);
            }
        };
        return Response.ok(stream)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=positions.kml").build();
    }

    @Path("csv")
    @GET
    @Produces("text/csv")
    public Response getCsv(
            @QueryParam("deviceId") long deviceId,
            @QueryParam("from") Date from, @QueryParam("to") Date to) throws StorageException {
        permissionsService.checkPermission(Device.class, getUserId(), deviceId);
        StreamingOutput stream = output -> {
            try {
                csvExportProvider.generate(output, deviceId, from, to);
            } catch (StorageException e) {
                throw new WebApplicationException(e);
            }
        };
        return Response.ok(stream)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=positions.csv").build();
    }

}
