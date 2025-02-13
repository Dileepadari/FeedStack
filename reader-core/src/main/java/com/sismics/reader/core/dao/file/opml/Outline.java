====FILE_DELIMITER====
package com.sismics.reader.core.dao.file.opml;

import java.util.ArrayList;
import java.util.List;

/**
 * OPML outline.
 *
 * @author jtremeaux
 */
public class Outline {
    private String text;
    private String title;
    private String type;
    private String xmlUrl;
    private String htmlUrl;
    private List<Outline> outlineList = new ArrayList<>();

    public Outline(String text, String title, String type, String xmlUrl, String htmlUrl, List<Outline> outlineList) {
        this.text = text;
        this.title = title;
        this.type = type;
        this.xmlUrl = xmlUrl;
        this.htmlUrl = htmlUrl;
        this.outlineList = outlineList;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public List<Outline> getOutlineList() {
        return outlineList;
    }
}