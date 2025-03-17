package com.sismics.reader.rest.resource;

import com.sismics.reader.core.util.EntityManagerUtil;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.dao.jpa.FeedDao;
import com.sismics.reader.core.model.jpa.Feed;
import com.sismics.reader.core.dao.jpa.ArticleDao;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.dao.jpa.dto.FeedDto;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.reader.core.service.FeedAdapter;

import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Iterator;
import java.util.logging.Logger;


@Path("/myfeeds")
public class MyFeedsResource extends BaseResource {


    @POST
    @Path("/getfeeds")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getFeeds(JSONObject data) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        String user_email = data.getString("userid");

        System.out.println("User email get: " + user_email);

        FeedDao feedDao = new FeedDao();
        List<Feed> feeds = feedDao.getByCreatorUserId(user_email);
        JSONArray response = new JSONArray();
        for (Feed feed : feeds) {
            JSONObject feedJson = new JSONObject();
            feedJson.put("id", feed.getId());
            feedJson.put("title", feed.getTitle());
            response.put(feedJson);
        }
        System.out.println(feeds);
        return Response.ok().entity(response).build();
    }

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createFeed(JSONObject data) throws JSONException {
        // Console.log("MyFeedsResource.createFeed: data = " + data.toString());
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        String user_email = data.getString("userid");
        String feed_title = data.getString("title");
        JSONObject article = data.getJSONObject("article");

        // show me all the keys in article object
        System.out.println("Article keys: ");
        Iterator<String> keys = article.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            System.out.println(key);
        }

        // create a url and local host link
        String link = "local : " + feed_title;
        String Baseuri = "http://localhost:8080/reader-web/";

        FeedDao feedDao = new FeedDao();
        // Create the feed
        Feed feed = new Feed();
        feed.setUrl(link);
        feed.setBaseUri(Baseuri);
        feed.setRssUrl(link);
        feed.setTitle(feed_title);
        feed.setLanguage("en-US");  
        feed.setDescription("Collections of favorites: " + feed_title);
        feed.setLastFetchDate(new Date());
        feed.setDeleteDate(null);
        feed.setCreatorUserId(user_email);
        feedDao.create(feed);
        EntityManagerUtil.flush();

        // Create the article
        Article newarticle = new Article();
        newarticle.setFeedId(feed.getId());
        newarticle.setGuid(UUID.randomUUID().toString());
        newarticle.setTitle(article.getString("title"));
        newarticle.setPublicationDate(new Date());
        newarticle.setUrl(article.getString("url"));
        newarticle.setCreator(article.getString("creator"));
        newarticle.setDescription(article.getString("description"));   
        ArticleDao articleDao = new ArticleDao();
        articleDao.create(newarticle);
        EntityManagerUtil.flush();

        // String rssFeed = generateRssContent(feed, newarticle);

        JSONObject response = new JSONObject();
        return Response.ok().entity(response).build();
    }

    // private String generateRssContent(Feed feed, Article article) {
    //     String rssFeed = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    //                         "<rss version=\"2.0\">\n" +
    //                         "xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"" +
    //                         "\n xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\"" +
    //                         "\n xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" +
    //                         "\n xmlns:atom=\"http://www.w3.org/2005/Atom\"" +
    //                         "\n xmlns:sy=\"http://purl.org/rss/1.0/modules/syndication/\"" +
    //                         "\n xmlns:slash=\"http://purl.org/rss/1.0/modules/slash/\">\n" +
    //                         "  <channel>\n" +
    //                         "    <title>" + feed.getTitle() + "</title>\n" +
    //                         "    <link>" + feed.getUrl() + "</link>\n" +
    //                         "    <description>" + feed.getDescription() + "</description>\n" +
    //                         "    <language>" + feed.getLanguage() + "</language>\n" +
    //                         "       <item>\n" +
    //                         "       <title>" + article.getTitle() + "</title>\n" +
    //                         "       <link>" + article.getUrl() + "</link>\n" +
    //                         "       <guid isPermaLink=\"false\"><![CDATA[" + article.getUrl() + "]]></guid>\n" +
    //                         "       <comments><![CDATA[" + article.getCommentUrl() + "]]></comments>\n" +
    //                         "       <slash:comments><![CDATA[" + article.getCommentCount() + "]]></slash:comments>\n" +
    //                         "       <dc:creator><![CDATA[" + article.getCreator() + "]]></dc:creator>\n" +
    //                         "       <pubDate>" + article.getPublicationDate() + "</pubDate>\n" +
    //                         "       <dc:date><![CDATA[" + article.getPublicationDate() + "]]></dc:date>\n" +
    //                         "       <description><![CDATA[" + article.getDescription() + "]]></description>\n" +
    //                         "       <content:encoded><![CDATA[" + article.getDescription() + "]]></content:encoded>\n" +
    //                         "    </item>\n" +
    //                         "  </channel>\n" +
    //                         "</rss>";
    //     return rssFeed;
    // }

    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addFeed(JSONObject data) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        System.out.println("Datadata: " + data);

        String user_email = data.getString("userid");
        String feed_id = data.getString("feedId");
        JSONObject article = data.getJSONObject("article");
        
        // Create the article
        Article newarticle = new Article();
        newarticle.setFeedId(feed_id);
        newarticle.setGuid(UUID.randomUUID().toString());
        newarticle.setTitle(article.getString("title"));
        newarticle.setPublicationDate(new Date());
        newarticle.setUrl(article.getString("url"));
        newarticle.setCreator(article.getString("creator"));
        newarticle.setDescription(article.getString("description"));   
        ArticleDao articleDao = new ArticleDao();
        articleDao.create(newarticle);
        EntityManagerUtil.flush();

        // // Get RssUrl from feed_id
        // FeedDao feedDao = new FeedDao();
        // Feed feed = feedDao.getFeedById(feed_id);
        // String rssUrl = feed.getRssUrl();

        // // Get the String Rss feed from the URL
        // String rssContent;
        // try {
        //     rssContent = fetchRssFeedContent(rssUrl);
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        // // Get String Rss of Article
        // String articleRss = articleToRssfeed(article);

        // // Combine the two Rss feeds
        // rssContent = rssContent.replace("</channel>", articleRss + "</channel>");

        // // Update the Rss feed Url
        // updateRssFeedContent(rssUrl, rssContent);

        JSONObject response = new JSONObject();
        return Response.ok().entity(response).build();
    }

    // private void updateRssFeedContent(String rssUrl, String rssContent) {
    //     URL url = new URL(rssUrl);
    //     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    //     connection.setRequestMethod("PUT");
    //     connection.setDoOutput(true);

    //     try (OutputStream os = connection.getOutputStream()) {
    //         byte[] input = rssContent.getBytes("utf-8");
    //         os.write(input, 0, input.length);
    //     }

    //     int responseCode = connection.getResponseCode();
    //     if (responseCode != HttpURLConnection.HTTP_OK) {
    //         throw new RuntimeException("Failed to update RSS feed content. HTTP response code: " + responseCode);
    //     }
    // }

    // private String fetchRssFeedContent(String rssUrl) {
    //     StringBuilder content = new StringBuilder();
    //     URL url = new URL(feedUrl);
    //     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    //     connection.setRequestMethod("GET");

    //     try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
    //         String inputLine;
    //         while ((inputLine = in.readLine()) != null) {
    //             content.append(inputLine);
    //         }
    //     }

    //     return content.toString();
    // }

    // private String articleToRssfeed(Article article) {
    //     String rssFeed = "<item>\n" +
    //                         "<title>" + article.getTitle() + "</title>\n" +
    //                         "<link>" + article.getUrl() + "</link>\n" +
    //                         "<guid isPermaLink=\"false\"><![CDATA[" + article.getUrl() + "]]></guid>\n" +
    //                         "<comments><![CDATA[" + article.getCommentUrl() + "]]></comments>\n" +
    //                         "<slash:comments><![CDATA[" + article.getCommentCount() + "]]></slash:comments>\n" +
    //                         "<dc:creator><![CDATA[" + article.getCreator() + "]]></dc:creator>\n" +
    //                         "<pubDate>" + article.getPublicationDate() + "</pubDate>\n" +
    //                         "<dc:date><![CDATA[" + article.getPublicationDate() + "]]></dc:date>\n" +
    //                         "<description><![CDATA[" + article.getDescription() + "]]></description>\n" +
    //                         "<content:encoded><![CDATA[" + article.getDescription() + "]]></content:encoded>\n" +
    //                         "</item>\n";
    //     return rssFeed;
    // }

    @POST
    @Path("/display")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response displayFeed(JSONObject data) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        String user_email = data.getString("userid");
        String feed_id = data.getString("feedId");

        ArticleDao articleDao = new ArticleDao();
        List<Article> articles = articleDao.findByFeed(feed_id);
        JSONArray response = new JSONArray();
        for (Article article : articles) {
            JSONObject articleJson = new JSONObject();
            articleJson.put("id", article.getId());
            articleJson.put("title", article.getTitle());
            articleJson.put("url", article.getUrl());
            articleJson.put("creator", article.getCreator());
            articleJson.put("description", article.getDescription());
            articleJson.put("commentUrl", article.getCommentUrl());
            articleJson.put("commentCount", article.getCommentCount());
            articleJson.put("publicationDate", article.getPublicationDate());
            response.put(articleJson);
        }
        return Response.ok().entity(response).build();
    }
}