package com.sismics.reader.rest.resource;

import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sismics.reader.core.dao.jpa.BugReportDao;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.rest.exception.ForbiddenClientException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.sismics.reader.core.constant.BugStatus;

// longValue

@Path("/bugs")
public class BugReportResource extends BaseResource {
    /**
     * Creates a bug report.
     *
     * @param bugReport Bug report
     * @return Response
     */
    @POST
    @Path("/report")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBugReport(String email, String description) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Create the bug report
        BugReportDto bugReport = new BugReportDto();
        // generate random identifier

        long id = java.util.UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        bugReport.setId(id);
        bugReport.setEmail(email);
        bugReport.setDescription(description);
        bugReport.setTimestamp(System.currentTimeMillis());
        bugReport.setStatus(BugStatus.OPEN);

        BugReportDao bugReportDao = new BugReportDao();
        bugReportDao.createBugReport(bugReport);

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
