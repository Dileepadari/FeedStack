package com.sismics.reader.rest.resource;

import com.sismics.reader.rest.dao.ThemeDao;
import com.sismics.rest.exception.ServerException;
import org.codehaus.jettison.json.JSONException;

import javax.servlet.ServletContext;
import java.util.List;




public class ThemeService {
    private final ThemeDao themeDao;

    public ThemeService() {
        this.themeDao = new ThemeDao();
    }

    public List<String> getThemes(ServletContext context) throws JSONException {
        try {
            return themeDao.findAll(context);
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Error getting theme list", e);
        }
    }
}