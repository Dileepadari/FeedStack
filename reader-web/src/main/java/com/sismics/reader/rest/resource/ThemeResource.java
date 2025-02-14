package com.sismics.reader.rest.resource;

import com.sismics.reader.rest.dao.ThemeDao;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.util.EnvironmentUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Theme REST resources.
 * 
 * @author jtremeaux
 */
@Path("/theme")
public class ThemeResource extends AuthenticatedResource {
    private final ThemeService themeService;

    public ThemeResource() {
        this.themeService = new ThemeService();
    }

    /**
     * Returns the list of all themes.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        // Only authenticate if needed
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get themes using service
        List<String> themeList = themeService.getThemes(
                EnvironmentUtil.isUnitTest() ? null : request.getServletContext()
        );

        // Format response
        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<>();
        for (String theme : themeList) {
            items.add(new JSONObject().put("id", theme));
        }
        response.put("themes", items);
        return Response.ok().entity(response).build();
    }
}