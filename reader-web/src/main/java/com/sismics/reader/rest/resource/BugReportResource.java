package com.sismics.reader.rest.resource;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sismics.reader.core.service.BugReportService;
import com.sismics.reader.rest.constant.BaseFunction;
import com.sismics.reader.core.dao.jpa.BugReportDao;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.rest.exception.ForbiddenClientException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.sismics.reader.core.constant.BugStatus;

import java.util.List;
import java.util.logging.Logger;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;

@Path("/bugs")
public class BugReportResource extends BaseResource {
    private BugReportService bugReportService;
    private static final Logger logger = Logger.getLogger(BugReportResource.class.getName());

    public BugReportResource() {
        this.bugReportService = new BugReportService();
    }

    @POST
    @Path("/report")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createBugReport(JSONObject data) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        String email = data.getString("email");
        String description = data.getString("description");

        bugReportService.createBugReport(email, description);

        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/getall")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBugReports() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        if (!hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ForbiddenClientException();
        }
        List<BugReportDto> bugReportList = bugReportService.getAllBugReports();
        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<>();
        for (BugReportDto bugReport : bugReportList) {
            JSONObject item = new JSONObject();
            item.put("id", bugReport.getId());
            item.put("email", bugReport.getEmail());
            item.put("description", bugReport.getDescription());
            item.put("timestamp", bugReport.getTimestamp());
            item.put("status", bugReport.getStatus());
            items.add(item);
        }
        response.put("items", items);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/getbyemail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBugReportsByEmail(@QueryParam("email") String email) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        List<BugReportDto> bugReportList = bugReportService.getBugReportsByEmail(email);
        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<>();
        for (BugReportDto bugReport : bugReportList) {
            JSONObject item = new JSONObject();
            item.put("id", bugReport.getId());
            item.put("email", bugReport.getEmail());
            item.put("description", bugReport.getDescription());
            item.put("timestamp", bugReport.getTimestamp());
            item.put("status", bugReport.getStatus());
            items.add(item);
        }
        response.put("items", items);
        return Response.ok().entity(response).build();
    }

    @POST
    @Path("/updatestatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateStatus(JSONObject data) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        if (!hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ForbiddenClientException();
        }
        String id = data.getString("id");
        String statusstr = data.getString("status");
        BugStatus status = BugStatus.valueOf(statusstr);

        bugReportService.updateBugReportStatus(id, status);

        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBugReport(@PathParam("id") String id) throws Exception {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        BugReportDto bugReport = bugReportService.getBugReport(id);
        if (bugReport == null) {
            throw new Exception("Bug report not found");
        }
        bugReportService.deleteBugReport(id);
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}