package com.sismics.reader.rest.resource;

import com.sismics.reader.core.dao.jpa.CategoryDao;
import com.sismics.reader.core.dao.jpa.FeedSubscriptionDao;
import com.sismics.reader.core.dao.jpa.UserArticleDao;
import com.sismics.reader.core.dao.jpa.criteria.FeedSubscriptionCriteria;
import com.sismics.reader.core.dao.jpa.criteria.UserArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.jpa.Category;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.reader.core.util.jpa.PaginatedLists;
import com.sismics.reader.rest.assembler.ArticleAssembler;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Category REST resources.
 * 
 * @author jtremeaux
 */
@Path("/category")
public class CategoryResource extends CategoryBaseResource {
    /**
     * Returns all categories.
     * 
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        validateAuthentication();

        // Get the root category
        CategoryDao categoryDao = new CategoryDao();
        Category rootCategory = categoryDao.getRootCategory(principal.getId());

        // Get the subcategories
        List<Category> categoryList = categoryDao.findSubCategory(rootCategory.getId(), principal.getId());

        // Build the response
        List<JSONObject> rootCategories = new ArrayList<JSONObject>();

        JSONObject rootCategoryJson = new JSONObject();
        rootCategoryJson.put("id", rootCategory.getId());
        rootCategories.add(rootCategoryJson);

        List<JSONObject> categoriesJson = new ArrayList<JSONObject>();
        for (Category category : categoryList) {
            categoriesJson.add(buildCategoryJson(category));
        }
        rootCategoryJson.put("categories", categoriesJson);

        JSONObject response = new JSONObject();
        response.put("categories", rootCategories);
        return Response.ok().entity(response).build();
    }

    /**
     * Returns all articles in a category.
     * 
     * @param id           Category ID
     * @param unread       Returns only unread articles
     * @param limit        Page limit
     * @param afterArticle Start the list after this article
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String id,
            @QueryParam("unread") boolean unread,
            @QueryParam("limit") Integer limit,
            @QueryParam("after_article") String afterArticle) throws JSONException {

        validateAuthentication();
      
        // Get the category
//        CategoryDao categoryDao = new CategoryDao();
        Category category = validateCategory(id);

        // Get the articles
        UserArticleDao userArticleDao = new UserArticleDao();
        UserArticleCriteria userArticleCriteria = new UserArticleCriteria()
                .setUnread(unread)
                .setUserId(principal.getId())
                .setSubscribed(true)
                .setVisible(true);
        if (category.getParentId() != null) {
            userArticleCriteria.setCategoryId(id);
        }
        PaginatedList<UserArticleDto> paginatedList = getPaginatedArticles(userArticleCriteria,limit, afterArticle);

        return buildArticleListResponse(paginatedList);
    }

    /**
     * Creates a new category.
     * 
     * @param name Category name
     * @return Response
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("name") String name) throws JSONException {

        validateAuthentication();

        // Validate input data

        name = ValidationUtil.validateLength(name, "name", 1, 100, false);

        // Get the root category
        CategoryDao categoryDao = new CategoryDao();
        Category rootCategory = categoryDao.getRootCategory(principal.getId());

        // Get the display order
        int displayOrder = categoryDao.getCategoryCount(rootCategory.getId(), principal.getId());

        // Create the category
        Category category = new Category();
        category.setUserId(principal.getId());
        category.setParentId(rootCategory.getId());
        category.setName(name);
        category.setOrder(displayOrder);
        String categoryId = categoryDao.create(category);

        JSONObject response = new JSONObject();
        response.put("id", categoryId);
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a category.
     * 
     * @param id Category ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        validateAuthentication();
        // Get the category
        Category category =  validateCategory(id);

        // Move subscriptions in this category to root
        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        List<FeedSubscription> feedSubscriptionList = feedSubscriptionDao.findByCategory(id);
        Category rootCategory = categoryDao.getRootCategory(principal.getId());
        for (FeedSubscription feedSubscription : feedSubscriptionList) {
            feedSubscription.setCategoryId(rootCategory.getId());
            feedSubscriptionDao.update(feedSubscription);
            feedSubscriptionDao.reorder(feedSubscription, 0);
        }

        // Delete the category
        categoryDao.delete(id);

        // Always return ok
        return Response.ok().entity(buildOkResponse()).build();
    }

    /**
     * Marks all articles in this category as read.
     * 
     * @param id Category ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(
            @PathParam("id") String id) throws JSONException {
        validateAuthentication();

        // Get the category
        Category category =  validateCategory(id);
        markCategoryArticlesAsRead(id);


        // Always return ok
        return Response.ok().entity(buildOkResponse()).build();
    }

    /**
     * Updates the category.
     * 
     * @param id     Category ID
     * @param name   Category name
     * @param order  Display order of this category
     * @param folded True if this category is folded in the subscriptions tree.
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("order") Integer order,
            @FormParam("folded") Boolean folded) throws JSONException {
        validateAuthentication();

        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 100, true);

        // Get the category
        Category category =  validateCategory(id);

        // Update the category
        if (name != null) {
            category.setName(name);
        }
        if (folded != null) {
            category.setFolded(folded);
        }
        categoryDao.update(category);

        // Reorder categories
        if (order != null) {
            categoryDao.reorder(category, order);
        }

        // Always return ok
        return Response.ok().entity(buildOkResponse()).build();
    }
}
