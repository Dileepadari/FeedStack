package com.sismics.reader.rest.resource;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sismics.reader.core.model.jpa.BugReport;
import com.sismics.reader.rest.constant.BaseFunction;
import com.sismics.reader.core.dao.jpa.BugReportDao;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.util.DateUtil;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.sismics.reader.core.constant.BugStatus;
import java.util.List;
import java.io.Console;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import java.util.Date;

@Path("/bugs")
public class BugReportResource extends BaseResource {
    /**
     * Creates a bug report.
     *
     * @param bugReport Bug report
     * @return Response
     */

    // var data = {
    // description: description,
    // email: email
    // };

    // // Sending bug report
    // $.ajax({
    // type: 'POST',
    // url: r.util.url.report_bug,
    // data: JSON.stringify(data),
    // contentType: 'application/json',
    // success: function(data) {
    // // Displaying success message
    // $().toastmessage('showSuccessToast', $.t('bugsreport.success'));
    // },
    // error: function(data) {
    // // Displaying error message
    // $().toastmessage('showErrorToast', $.t('bugsreport.error'));
    // }
    // });

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

        // Create the bug report
        BugReport bugReport = new BugReport();
        Date timeStamp = new Date(System.currentTimeMillis());
        bugReport.setId(java.util.UUID.randomUUID().toString());
        bugReport.setEmail(email);
        bugReport.setDescription(description);
        bugReport.setTimestamp(timeStamp);
        bugReport.setStatus(BugStatus.OPEN);

        BugReportDao bugReportDao = new BugReportDao();
        bugReportDao.createBugReport(bugReport);

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/getall")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllBugReports() throws JSONException {
        // Only authenticate if needed
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        // check admin permissions
        if (!hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ForbiddenClientException();
        }
        // Get all bug reports using service
        BugReportDao bugReportDao = new BugReportDao();
        List<BugReportDto> bugReportList = bugReportDao.getAllBugReports();
        // Convert to JSON
        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
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
        // Only authenticate if needed
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get bug reports by email using service
        BugReportDao bugReportDao = new BugReportDao();
        List<BugReportDto> bugReportList = bugReportDao.getBugReportsByEmail(email);
        // Convert to JSON
        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
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

        // check admin permissions
        if (!hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ForbiddenClientException();
        }

        // data = {"id":"666db9bc-4c40-4b49-a0da-2b25a7ec8325","status":"CLOSED"}
        String id = data.getString("id");
        String statusstr = data.getString("status");
        BugStatus status = BugStatus.valueOf(statusstr);

        BugReportDao bugReportDao = new BugReportDao();
        bugReportDao.updateBugReportStatus(id, status);

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBugReport(@PathParam("id") String id) throws Exception {
        // Only authenticate if needed
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the bug report
        BugReportDao bugReportDao = new BugReportDao();
        BugReportDto bugReport = bugReportDao.getBugReport(id);
        if (bugReport == null) {
            throw new Exception("Bug report not found");
        }
        bugReportDao.deleteBugReport(id);
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}