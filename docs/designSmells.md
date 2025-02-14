# Task 2A - Design Smells

## Tools

### SonarQube Report
![SonarQube Report](sonarqube/image.png)

Eventhough SonarQube identified 424 code smells, almost all of them are mostly code related smells and not design smells. (Ex: Try-catch blocks, refactoring if statements, etc.)

### DesigniteJava Report

Design Smells identified by DesigniteJava are included in the file `docs/designite/designCodeSmells.csv`

## Design Smell Analysis

### 1. Broken Modularization
**Location:** `com.sismics.reader.core.constant`

**Problem:**
- Constants unrelated to each other are grouped in the same class
- Violates Single Responsibility Principle

**Solution:**
1. Separate constants into domain-specific classes
2. Group related constants together

**Quality Impact:**
- Maintainability: Changes in one concern affect unrelated areas
- Scalability: Adding features requires modifying a single class
- Readability: Mixed concerns reduce code clarity
- Reusability: Unnecessary dependencies between modules

**Code Changes:**

Before: (in `com.sismics.reader.core.constant.Constants`)
```java
public class Constants {
   /**
     * Default locale.
     */
    public static final String DEFAULT_LOCALE_ID = "en";
    /**
     * Default timezone ID.
     */
    public static final String DEFAULT_TIMEZONE_ID = "Europe/London";
    ....

}
```

Here, not only there are many constants, but also they are not related to each other. There are different groups of constants in the same class.

After:

4 different classes are created:

- `com.sismics.reader.core.constant.DefaultConfig`
- `com.sismics.reader.core.constant.SecurityConfig`
- `com.sismics.reader.core.constant.LuceneConfig`
- `com.sismics.reader.core.constant.ImportJobConfig`

Each of these classes are responsible for a single concern and are not mixed with other concerns. This makes the code more maintainable and easier to understand. It is also easier to add new constants without affecting other classes.

**LLM Suggestions:**

Prompt:
`
I will provide code, type of design smell. Justify the reason why that happens, also indicate where exactly in the code it happens. Explain the quality attributes affected by that smell, and also indicate the steps to refactor it.
`

- ChatGPT:
![ChatGPT](llm_responses/smell-1/gpt.png)

Here the change suggested by ChatGPT is correct, the actual change made is the same. This is a relatively simple refactoring, so the LLM was able to suggest the correct solution.

### 2. Cyclic Dependency
**Location:** 
- `com.sismics.reader.core.service.FeedService`
- `com.sismics.reader.core.service.IndexingService`

**Problem:**

Multiple circular dependencies:
- AppContext ↔️ FeedService ↔️ AppContext
- AppContext ↔️ IndexingService ↔️ AppContext

**Solution:**

I have added an interface that takes both **AppContext**,**FeedService** and takes care of posting events to the listeners in the **AppContext**.

1. **Mediator** interface
```java
package com.sismics.reader.core.mediator;

public interface Mediator {
    void notify(Object sender,Object event);
}

```

2. **ConcreteMediator** class

```java 
package com.sismics.reader.core.mediator;

import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.model.context.AppContext;

public  class ConcreteMediator implements Mediator {
    private AppContext appContext;
    private FeedService feedService;

    public ConcreteMediator(AppContext appContext, FeedService feedService) {
        this.appContext = appContext;
        this.feedService = feedService;
    }

    @Override
    public void notify(Object sender, Object event) {
        if (sender instanceof FeedService) {
           appContext.getInstance().getEventBus().post(event);
        }
    }
}
```


**Code Changes:**

- Before Refactoring step:

1. **AppContext** class
```java

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.listener.async.*;
import com.sismics.reader.core.listener.sync.DeadEventListener;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.util.EnvironmentUtil;

private AppContext() {
    resetEventBus();
    
    feedService = new FeedService();
    feedService.startAndWait();
    
    ConfigDao configDao = new ConfigDao();
    Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
    indexingService = new IndexingService(luceneStorageConfig != null ? luceneStorageConfig.getValue() : null);
    indexingService.startAndWait();
}
```

2. **FeedService** class

```java
import com.sismics.reader.core.event.ArticleCreatedAsyncEvent;
import com.sismics.reader.core.event.ArticleDeletedAsyncEvent;
import com.sismics.reader.core.event.ArticleUpdatedAsyncEvent;
import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
import com.sismics.reader.core.model.context.AppContext;

List<Article> articleToRemove = getArticleToRemove(articleList);
    if (!articleToRemove.isEmpty()) {
        for (Article article : articleToRemove) {
            // Update unread counts
            // FIXME count be optimized in 1 query instead of a*s*2
            List<UserArticleDto> userArticleDtoList = new UserArticleDao()
                    .findByCriteria(new UserArticleCriteria()
                            .setArticleId(article.getId())
                            .setFetchAllFeedSubscription(true) // to test: subscribe another user, u2, read u1, not u2, u1 is decremented anyway
                            .setUnread(true));

            for (UserArticleDto userArticleDto : userArticleDtoList) {
                FeedSubscriptionDto feedSubscriptionDto = new FeedSubscriptionDao().findFirstByCriteria(new FeedSubscriptionCriteria()
                        .setId(userArticleDto.getFeedSubscriptionId()));
                if (feedSubscriptionDto != null) {
                    new FeedSubscriptionDao().updateUnreadCount(feedSubscriptionDto.getId(), feedSubscriptionDto.getUnreadUserArticleCount() - 1);
                }
            }
        }

        // Delete articles that don't exist anymore
        for (Article article: articleToRemove) {
            new ArticleDao().delete(article.getId());
        }

        // Removed articles from index
        ArticleDeletedAsyncEvent articleDeletedAsyncEvent = new ArticleDeletedAsyncEvent();
        articleDeletedAsyncEvent.setArticleList(articleToRemove);
        AppContext.getInstance().getAsyncEventBus().post(articleDeletedAsyncEvent);
    }
```


- After Refactoring step:
1. **AppContext** class
```java
private AppContext() {
    resetEventBus();
    
    mediator = new ConcreteMediator(this, feedService);

    feedService = new FeedService(mediator);
    feedService.startAndWait();
    
    ConfigDao configDao = new ConfigDao();
    Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
    indexingService = new IndexingService(luceneStorageConfig != null ? luceneStorageConfig.getValue() : null, mediator);
    indexingService.startAndWait();
}
```

2. **FeedService** class

```java
import com.sismics.reader.core.event.ArticleCreatedAsyncEvent;
import com.sismics.reader.core.event.ArticleDeletedAsyncEvent;
import com.sismics.reader.core.event.ArticleUpdatedAsyncEvent;
import com.sismics.reader.core.event.FaviconUpdateRequestedEvent;
import com.sismics.reader.core.mediator.Mediator;

private Mediator mediator;

public FeedService(Mediator mediator) {
    this.mediator = mediator;
}

List<Article> articleToRemove = getArticleToRemove(articleList);
if (!articleToRemove.isEmpty()) {
    for (Article article : articleToRemove) {
        // Update unread counts
        // FIXME count be optimized in 1 query instead of a*s*2
        List<UserArticleDto> userArticleDtoList = new UserArticleDao()
                .findByCriteria(new UserArticleCriteria()
                        .setArticleId(article.getId())
                        .setFetchAllFeedSubscription(true) // to test: subscribe another user, u2, read u1, not u2, u1 is decremented anyway
                        .setUnread(true));

        for (UserArticleDto userArticleDto : userArticleDtoList) {
            FeedSubscriptionDto feedSubscriptionDto = new FeedSubscriptionDao().findFirstByCriteria(new FeedSubscriptionCriteria()
                    .setId(userArticleDto.getFeedSubscriptionId()));
            if (feedSubscriptionDto != null) {
                new FeedSubscriptionDao().updateUnreadCount(feedSubscriptionDto.getId(), feedSubscriptionDto.getUnreadUserArticleCount() - 1);
            }
        }
    }

    // Delete articles that don't exist anymore
    for (Article article: articleToRemove) {
        new ArticleDao().delete(article.getId());
    }

    // Removed articles from index
    ArticleDeletedAsyncEvent articleDeletedAsyncEvent = new ArticleDeletedAsyncEvent();
    articleDeletedAsyncEvent.setArticleList(articleToRemove);
    mediator.notify(this, articleDeletedAsyncEvent);
}
```

**Issues**:

1. With this fix it is creating a new issue for search functionality. Rebuilding the index in IndexingService is not working as expected. So, left the issue open for further investigation.

**llm Suggestions:**

Prompt : AppContext initalizes feedservice, eventbuses which listens the events. Feedservice calls appcontext and posts the events. So, this forms circular dependency right.

Reply :
Yes, the current design forms a circular dependency where AppContext initializes FeedService, and FeedService calls back to AppContext to post events. This circular dependency can be problematic and should be refactored.

Refactoring with **Dependency Injection**
To break this circular dependency, we can use dependency injection to manage the dependencies and ensure that FeedService does not directly depend on AppContext. Instead, we can inject the necessary dependencies into FeedService.

![fig1](llm_responses/smell-2/fig1.png)
![fig2](llm_responses/smell-2/fig2.png)
![fig3](llm_responses/smell-2/fig3.png)
![fig4](llm_responses/smell-2/fig4.png)

### 3. Deficient Encapsulation
**Location:**
- `com.sismics.reader.core.dao.file.rss.RssReader`
- `com.sismics.reader.core.dao.file.html.FaviconDownloader`

**Problem:**
- Public fields expose internal state
- Direct exposure of internal data structures
- Improper access modifiers

**Solution:**
1. Make instance variables private
2. Implement defensive copying
3. Create immutable value objects

**Quality Impact:**
- Maintainability: Implementation changes affect external components
- Security: Vulnerable to state manipulation

### 4. Insufficient Modularization

**Location:**
- `com.sismics.reader.core.dao.jpa.dto.UserArticleDto`
- `com.sismics.reader.core.dao.jpa.dto.ArticleDto`
- `com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto`

**Problem:**
- **Mixed Responsibilities:** DTOs contained fields for multiple domain concerns (e.g., article data mixed with comment and enclosure details).
- **Tight Coupling:** Changes in one concern (such as comments) required modifications in several DTOs, increasing the risk of errors.
- **Code Duplication:** Similar fields (like `commentCount`, `enclosureType`, etc.) were repeated across different DTOs, making the code hard to maintain and extend.

**Solution:**
1. **Split DTOs by Domain Concept:**  
   Create new domain-specific DTOs such as:
   - `CommentDto`
   - `EnclosureDto`
   - `CategoryDto`
   - `FeedDto`
2. **Refactor Existing DTOs:**  
   Update classes like `ArticleDto` and `FeedSubscriptionDto` to delegate responsibilities to these new DTOs.  
   **Example:**  
   - **Old Approach:**  
     ```java
     public class ArticleDto {
         private String commentUrl;
         private Integer commentCount;
         private String enclosureUrl;
         // Other fields...
     }
     ```
   - **New Approach:**  
     ```java
     public class ArticleDto {
         private CommentDto comment;
         private EnclosureDto enclosure;
         // Other fields...
     }
     ```
3. **Update Mappers:**  
   Refactor mappers (e.g., `ArticleMapper`) to map data into nested DTOs.  
   **Example:**  
   - **Old Mapping:**  
     ```java
     dto.setCommentUrl(stringValue(o[i++]));
     dto.setCommentCount(intValue(o[i++]));
     ```
   - **New Mapping:**  
     ```java
     CommentDto commentDto = new CommentDto();
     commentDto.setUrl(stringValue(o[i++]));
     commentDto.setCount(intValue(o[i++]));
     dto.setComment(commentDto);
     ```
4. **Revise Service and REST Layers:**  
   Update assemblers and service methods to access data via the nested DTOs.  
   **Example:**  
   - **Old:**  
     ```java
     userArticleJson.put("comment_url", userArticle.getArticle().getCommentUrl());
     ```
   - **New:**  
     ```java
     userArticleJson.put("comment_url", userArticle.getArticle().getComment().getUrl());
     ```

**Quality Impact:**
- **Maintainability:**  
  Isolating concerns means that modifications in one domain (e.g., comments) only affect the corresponding DTO, reducing the risk of unintended side effects.
- **Reusability:**  
  Domain-specific DTOs are more generic and can be reused across various parts of the application without duplicating code.
- **Testability:**  
  Smaller, focused DTOs simplify unit testing, as tests can target individual components rather than large, composite objects.
- **Flexibility:**  
  The modular structure makes it easier to add new features or modify existing ones without affecting unrelated components.

---

#### Detailed Comparison: Old vs. Refactored Code

| **Aspect**         | **Old Code**                                                                                                           | **New Code**                                                                                                                                                   | **Why It’s Better**                                                                                                                                                                   |
|--------------------|------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Responsibilities** | Mixed article data with comment/enclosure fields (e.g., `commentUrl`, `enclosureUrl`).                                  | Split into domain-specific DTOs: `ArticleDto` now uses `CommentDto`, `EnclosureDto`, etc.                                                                    | Each DTO now has a single responsibility, reducing complexity.                                                                                                                     |
| **Coupling**          | Tight coupling: Changes to comment/enclosure details required modifying multiple DTOs.                                | Loose coupling: `ArticleDto` depends on abstracted DTOs like `CommentDto`, `EnclosureDto`.                                                                   | Decouples concerns—changes in one area (like comments) only affect its corresponding DTO.                                                                                            |
| **Code Duplication**  | Fields like `commentCount` and `enclosureType` were duplicated across several DTOs.                                    | Reusable DTOs (e.g., `CommentDto` is used wherever comment data is needed).                                                                                    | Eliminates duplication, thereby improving consistency and reducing maintenance effort.                                                                                               |
| **Data Structure**    | Flat structure with all properties directly embedded in a single DTO.                                                | Hierarchical structure with nested DTOs (e.g., `article.getComment().getUrl()`).                                                                              | More accurately models real-world relationships and improves readability.                                                                                                            |
| **Maintainability**   | Modifications to comment logic required changes in multiple DTOs and mappers.                                           | Changes are localized: updating `CommentDto` automatically propagates to all DTOs that utilize it.                                                              | Simplifies maintenance and minimizes the risk of errors.                                                                                                                             |
| **Testability**       | Testing involved constructing large objects with numerous unrelated fields.                                          | Smaller, focused DTOs allow for targeted unit tests in isolation.                                                                                             | Enhances test coverage by allowing easier and more precise unit tests.                                                                                                                 |
| **Flexibility**       | Adding or removing fields (like `isFolded` in categories) required changes across multiple layers.                   | New features can be added by extending only the relevant DTO (e.g., `CategoryDto` now includes `isFolded`), without impacting other components.             | Increases adaptability to new requirements, as changes are isolated within specific domain areas.                                                                                      |

---

**LLM Suggestions:**

Prompt:
```bash
I will provide code, type of design smell. Justify the reason why that happens, also indicate where exactly in the code it happens. Explain the quality attributes affected by that smell, and also indicate the steps to refactor it.
```

- ChatGPT:
![ChatGPT](llm_responses/smell-4/gpt-input.png)

- Response:
![ChatGPT](llm_responses/smell-4/gpt-output1.png)
![ChatGPT](llm_responses/smell-4/gpt-output2.png)
![ChatGPT](llm_responses/smell-4/gpt-output3.png)


#### Conclusion

By refactoring the DTOs to enforce proper modularization, we directly address the issues of mixed responsibilities, tight coupling, and code duplication. This change aligns with the Single Responsibility Principle, leading to a cleaner, more maintainable, and testable codebase. Future changes—such as updating comment details or extending feed information—will be isolated to their respective DTOs, thereby reducing the risk of unintended side effects and streamlining the overall development process.

### 5. Broken Heirarchy
**Location:** `com.sismics.reader.rest.resource.ThemeResource`

**Problem:**
- ThemeResource extends BaseResource but doesn't effectively use the inheritance hierarchy.
- ThemeResource extends BaseResource but only uses authentication functionality while inheriting unnecessary methods like checkBaseFunction and hasBaseFunction.
- It only uses the authentication check but doesn't leverage other base class functionalities.
- This creates unnecessary coupling and violates the Liskov Substitution Principle.


**Solution:**
1. Create focused base class with only authentication
2. Remove unnecessary inheritance from BaseResource
3. Extend only the minimal required base class
4. Keep only the functionality needed for theme managements

**Quality Attributes Affected:**

1. Maintainability
    - Harder to maintain due to unnecessary inherited methods
    - Changes to BaseResource may unexpectedly impact ThemeResource
    - More complex dependency relationships to manage
2. Reusability
    - Tight coupling makes it difficult to reuse components independently
    - ThemeResource is less portable due to unnecessary dependencies
    - Cannot easily reuse theme functionality in other contexts
3. Testability
    - More difficult to unit test due to unnecessary inherited behavior
    - Need to mock/stub unused functionality
    - Testing requires understanding full inheritance chain
4. Understandability
    - Developers must understand full BaseResource functionality
    - Unclear which inherited methods are actually used
    - More complex code navigation

**Code Changes:**

1. Created new ThemeService:
```java
public class ThemeService {
    private final ThemeDao themeDao;
    
    public ThemeService() {
        this.themeDao = new ThemeDao();
    }
    
    public List<String> getThemes(ServletContext context) {
        try {
            return themeDao.findAll(context);
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Error getting theme list", e);
        }
    }
}
```

This is to create a more focused theme service.

2.  Create a new AuthenticatedResource Class, which acts as a BaseResource with only Authentication.
```java
public class ThemeService {
    private final ThemeDao themeDao;
    
    public ThemeService() {
        this.themeDao = new ThemeDao();
    }
    
    public List<String> getThemes(ServletContext context) {
        try {
            return themeDao.findAll(context);
        } catch (Exception e) {
            throw new ServerException("UnknownError", "Error getting theme list", e);
        }
    }
}
```

3. Refactoring ThemeResource according to these new classes:
```java
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
```
**LLM Suggestions:**

Prompt:
`
I will provide code, type of design smell. Justify the reason why that happens, also indicate where exactly in the code it happens. Explain the quality attributes affected by that smell, and also indicate the steps to refactor it.

`

- Claude:
![Claude](llm_responses/smell-5/image.png)

While the refactoring is correct, There could be an additional layer of abstraction (ThemeService) to make it more modular and easier to maintain.


### 6. Wide Hierarchy
**Location:** `com.sismics.reader.rest.resource.BaseResource`

**Problems:**
- Too many direct subclasses
- Lack of intermediate abstractions
- Poor organization of endpoint responsibilities

**Solution:**
1. Introduce intermediate abstract classes to group related functionality.
2. Organize resources based on concerns (e.g., authentication, pagination, validation).
3. Standardize response handling and error management.

**Quality Impact:**
- **Maintainability:** More consistent and predictable code structure.
- **Reliability:** Unified error handling improves robustness.
- **Security:** Consistent authentication mechanisms enhance security.

---

## Code Refactoring
### Before Refactoring (Code Duplication & Inefficiencies)
![Before Refactoring](./llm_responses/smell-5-6/beforerefactoring.png)
```java
public Response read(@PathParam("id") String id) throws JSONException {
    if (!authenticate()) {
        throw new ForbiddenClientException();
    }
    
    // Fetch article
    UserArticleDao userArticleDao = new UserArticleDao();
    UserArticle userArticle = userArticleDao.getUserArticle(id, principal.getId());
    if (userArticle == null) {
        throw new ClientException("ArticleNotFound", MessageFormat.format("Article not found: {0}", id));
    }
    
    if (userArticle.getReadDate() == null) {
        // Mark article as read
        userArticle.setReadDate(new Date());
        userArticleDao.update(userArticle);
        
        // Update unread count
        FeedSubscriptionDao feedSubscriptionDao = new FeedSubscriptionDao();
        for (FeedSubscriptionDto feedSubscription : feedSubscriptionDao.findByCriteria(
            new FeedSubscriptionCriteria().setFeedId(userArticle.getArticleId()).setUserId(principal.getId()))) {
            feedSubscriptionDao.updateUnreadCount(feedSubscription.getId(), feedSubscription.getUnreadUserArticleCount() - 1);
        }
    }
    
    // Return response
    JSONObject response = new JSONObject();
    response.put("status", "ok");
    return Response.ok().entity(response).build();
}
```

### After Refactoring (Simplified & Modularized Code)
![After Refactoring](./llm_responses/smell-5-6/afterRefactoring.png)
```java
public Response read(@PathParam("id") String id) throws JSONException {
    validateAuthentication();
    updateReadStatus(id, true);
    return Response.ok().entity(buildOkResponse()).build();
}
```

**Refactored Enhancements:**
- **`validateAuthentication()`**: Centralized authentication logic (used in 40+ instances).
- **`updateReadStatus(id, true)`**: Modularized logic for marking articles as read.
- **`buildOkResponse()`**: Standardized JSON response format.

---

## Structural Improvements

### Before Refactoring
- All resource classes directly inherited from `BaseResource`.
- Redundant authentication, pagination, and error handling in multiple classes.

### After Refactoring
- Introduced intermediate abstract classes for common functionality.
- Reduced code duplication by centralizing repetitive operations.
- Standardized authentication, response building, and error handling.

## Key Benefits
### 1. **Reduced Code Duplication**
✅ Centralized authentication logic  
✅ Shared pagination handling  
✅ Unified validation and response building  

### 2. **Better Organization**
✅ Clear separation of concerns  
✅ Logical grouping of related functionalities  
✅ Consistent method structuring  

### 3. **Enhanced Maintainability**
✅ Smaller, focused classes  
✅ Reduced method complexity  
✅ Easier testing and debugging  
✅ Simplified feature addition  

### 4. **Standardized Operations**
✅ Consistent validation patterns  
✅ Uniform error handling  
✅ Standardized response formats  
✅ Reusable utility methods  

## Common Operations in the Refactored Hierarchy
- **Authentication validation**: Centralized authentication checks.
- **Article pagination**: Standardized pagination for article retrieval.
- **Response building**: Consistent JSON response format.
- **Error handling**: Unified error-handling mechanisms.
- **Category & subscription management**: Reusable methods for managing categories and subscriptions.

---

## Conclusion
The refactoring of `BaseResource` and its subclasses has significantly improved code maintainability, readability, and security. By introducing intermediate abstract classes and standardizing key operations, we have:
- Eliminated unnecessary code duplication.
- Established a clear and modular structure.
- Strengthened authentication and error-handling consistency.
- Made future enhancements and testing easier.

This structured approach ensures long-term scalability and a cleaner architecture for the `com.sismics.reader.rest.resource` package.


### 7. Feature Envy

**Location:** `com.sismics.reader.rest.resource.UserResource`

**Problem:**

- UserResource is too dependent on the data and functionality of the User class.
- It creates and sets up a new User object with many setter calls, suggesting this logic might belong in the User class instead.
- The update methods (both versions) show strong feature envy towards the User class. They make multiple calls to set various user properties

**Solution:**

1. Move User creation and update logic to the User class
2. Refactor UserResource to use the User class directly
3. Implement proper encapsulation and abstraction

**Quality Attributes Affected:**

1. Maintainability
    - Reduced code duplication
    - Improved separation of concerns
    - Easier to maintain and extend

2. Testability
    - More focused unit tests
    - Reduced test complexity
    - Improved test coverage

3. Understandability
    - Reduced cognitive complexity

**Code Changes:**

1. User Class is updated to include the creation and update logic:
```java
public static User createNewUser(String username, String password, String email, String localeId) {
    User user = new User();
    user.setRoleId(SecurityConfig.DEFAULT_USER_ROLE);
    user.setUsername(username);
    user.setPassword(password);
    user.setEmail(email);
    user.setDisplayTitleWeb(false);
    user.setDisplayTitleMobile(true);
    user.setDisplayUnreadWeb(true);
    user.setDisplayUnreadMobile(true);
    user.setCreateDate(new Date());
    user.setLocaleId(localeId);
    return user;
}
```

```java

// In User.java
public void updateProperties(String email, String themeId, String localeId, 
                           Boolean displayTitleWeb, Boolean displayTitleMobile,
                           Boolean displayUnreadWeb, Boolean displayUnreadMobile,
                           Boolean narrowArticle) {
    if (email != null) this.setEmail(email);
    if (themeId != null) this.setTheme(themeId);
    if (localeId != null) this.setLocaleId(localeId);
    if (displayTitleWeb != null) this.setDisplayTitleWeb(displayTitleWeb);
    if (displayTitleMobile != null) this.setDisplayTitleMobile(displayTitleMobile);
    if (displayUnreadWeb != null) this.setDisplayUnreadWeb(displayUnreadWeb);
    if (displayUnreadMobile != null) this.setDisplayUnreadMobile(displayUnreadMobile);
    if (narrowArticle != null) this.setNarrowArticle(narrowArticle);
}
```

2. UserResource is updated to use the User class directly:
```java
@PUT
@Produces(MediaType.APPLICATION_JSON)
public Response register(
    @FormParam("username") String username,
    @FormParam("password") String password,
    @FormParam("locale") String localeId,
    @FormParam("email") String email) throws JSONException {
        ...

    User user = User.createNewUser(username, password, email, localeId);

    ...
@POST
@Produces(MediaType.APPLICATION_JSON)
public Response update(
    @FormParam("password") String password,
    @FormParam("email") String email,
    @FormParam("theme") String themeId,
    @FormParam("locale") String localeId,
    @FormParam("display_title_web") Boolean displayTitleWeb,
    @FormParam("display_title_mobile") Boolean displayTitleMobile,
    @FormParam("display_unread_web") Boolean displayUnreadWeb,
    @FormParam("display_unread_mobile") Boolean displayUnreadMobile,
    @FormParam("narrow_article") Boolean narrowArticle,
    @FormParam("first_connection") Boolean firstConnection) throws JSONException {

    ...

    user.updateProperties(email, themeId, localeId, displayTitleWeb, 
                         displayTitleMobile, displayUnreadWeb, 
                         displayUnreadMobile, narrowArticle);

    ...
}
```

**LLM Suggestions:**

Prompt:
`
I will provide code, type of design smell. Justify the reason why that happens, also indicate where exactly in the code it happens. Explain the quality attributes affected by that smell, and also indicate the steps to refactor it.
`

![ChatGPT](llm_responses/smell-7/image.png)
![ChatGPT](llm_responses/smell-7/image_copy.png)

It gave the same solution as I did. However, I did not consider the setPassword method in the User class.

### 8. God Object
**Location:**
- `com.sismics.reader.core.model.context.AppContext.java`

**Problem:**
- The AppContext class is handling too many responsibilities, including managing services, event buses, and configuration. This violates the Single Responsibility Principle.

**Solution:**
- Break down AppContext into smaller, more focused classes that handle specific responsibilities, such as a dedicated service manager or event bus manager.

**Code changes:**
Before Refactoring step:
```java
package com.sismics.reader.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.listener.async.*;
import com.sismics.reader.core.listener.sync.DeadEventListener;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.util.EnvironmentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Global application context.
 *
 * @author jtremeaux
 */
public class AppContext {
    /**
     * Singleton instance.
     */
    private static AppContext instance;

    /**
     * Event bus.
     */
    private EventBus eventBus;

    /**
     * Generic asynchronous event bus.
     */
    private EventBus asyncEventBus;

    /**
     * Asynchronous event bus for emails.
     */
    private EventBus mailEventBus;

    /**
     * Asynchronous event bus for mass imports.
     */
    private EventBus importEventBus;

    /**
     * Feed service.
     */
    private FeedService feedService;

    /**
     * Indexing service.
     */
    private IndexingService indexingService;

    /**
     * Asynchronous executors.
     */
    private List<ExecutorService> asyncExecutorList;

    /**
     * Private constructor.
     */
    private AppContext() {
        resetEventBus();

        feedService = new FeedService();
        feedService.startAndWait();

        ConfigDao configDao = new ConfigDao();
        Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
        indexingService = new IndexingService(luceneStorageConfig != null ? luceneStorageConfig.getValue() : null);
        indexingService.startAndWait();
    }

    /**
     * (Re)-initializes the event buses.
     */
    private void resetEventBus() {
        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());

        asyncExecutorList = new ArrayList<ExecutorService>();

        asyncEventBus = newAsyncEventBus();
        asyncEventBus.register(new ArticleCreatedAsyncListener());
        asyncEventBus.register(new ArticleUpdatedAsyncListener());
        asyncEventBus.register(new ArticleDeletedAsyncListener());
        asyncEventBus.register(new RebuildIndexAsyncListener());
        asyncEventBus.register(new FaviconUpdateRequestedAsyncListener());

        mailEventBus = newAsyncEventBus();

        importEventBus = newAsyncEventBus();
        importEventBus.register(new SubscriptionImportAsyncListener());
    }

    /**
     * Returns a single instance of the application context.
     * 
     * @return Application context
     */
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    /**
     * Wait for termination of all asynchronous events.
     * /!\ Must be used only in unit tests and never a multi-user environment.
     */
    public void waitForAsync() {
        if (EnvironmentUtil.isUnitTest()) {
            return;
        }
        try {
            for (ExecutorService executor : asyncExecutorList) {
                // Shutdown executor, don't accept any more tasks (can cause error with nested
                // events)
                try {
                    executor.shutdown();
                    executor.awaitTermination(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    // NOP
                }
            }
        } finally {
            resetEventBus();
        }
    }

    /**
     * Creates a new asynchronous event bus.
     * 
     * @return Async event bus
     */
    private EventBus newAsyncEventBus() {
        if (EnvironmentUtil.isUnitTest()) {
            return new EventBus();
        } else {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            asyncExecutorList.add(executor);
            return new AsyncEventBus(executor);
        }
    }

    /**
     * Getter of eventBus.
     *
     * @return eventBus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Getter of asyncEventBus.
     *
     * @return asyncEventBus
     */
    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    /**
     * Getter of mailEventBus.
     *
     * @return mailEventBus
     */
    public EventBus getMailEventBus() {
        return mailEventBus;
    }

    /**
     * Getter of importEventBus.
     *
     * @return importEventBus
     */
    public EventBus getImportEventBus() {
        return importEventBus;
    }

    /**
     * Getter of feedService.
     *
     * @return feedService
     */
    public FeedService getFeedService() {
        return feedService;
    }

    /**
     * Getter of indexingService.
     *
     * @return indexingService
     */
    public IndexingService getIndexingService() {
        return indexingService;
    }
}
```

After Refactoring step:
1. **EventBusManager.java**
```java
package com.sismics.reader.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.listener.async.*;
import com.sismics.reader.core.listener.sync.DeadEventListener;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.util.EnvironmentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventBusManager {
    /**
     * Event bus.
     */
    private EventBus eventBus;

    /**
     * Generic asynchronous event bus.
     */
    private EventBus asyncEventBus;

    /**
     * Asynchronous event bus for emails.
     */
    private EventBus mailEventBus;

    /**
     * Asynchronous event bus for mass imports.
     */
    private EventBus importEventBus;

    /**
     * Asynchronous executors.
     */
    private List<ExecutorService> asyncExecutorList;

    public EventBusManager() {
        resetEventBus();
    }

    private void resetEventBus() {
        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());

        asyncExecutorList = new ArrayList<ExecutorService>();

        asyncEventBus = newAsyncEventBus();
        asyncEventBus.register(new ArticleCreatedAsyncListener());
        asyncEventBus.register(new ArticleUpdatedAsyncListener());
        asyncEventBus.register(new ArticleDeletedAsyncListener());
        asyncEventBus.register(new RebuildIndexAsyncListener());
        asyncEventBus.register(new FaviconUpdateRequestedAsyncListener());

        mailEventBus = newAsyncEventBus();

        importEventBus = newAsyncEventBus();
        importEventBus.register(new SubscriptionImportAsyncListener());
    }

    /**
     * Wait for termination of all asynchronous events.
     * /!\ Must be used only in unit tests and never a multi-user environment.
     */
    public void waitForAsync() {
        if (EnvironmentUtil.isUnitTest()) {
            return;
        }
        try {
            for (ExecutorService executor : asyncExecutorList) {
                // Shutdown executor, don't accept any more tasks (can cause error with nested
                // events)
                try {
                    executor.shutdown();
                    executor.awaitTermination(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    // NOP
                }
            }
        } finally {
            resetEventBus();
        }
    }

    /**
     * Creates a new asynchronous event bus.
     * 
     * @return Async event bus
     */
    private EventBus newAsyncEventBus() {
        if (EnvironmentUtil.isUnitTest()) {
            return new EventBus();
        } else {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            asyncExecutorList.add(executor);
            return new AsyncEventBus(executor);
        }
    }

    /**
     * Getter of eventBus.
     *
     * @return eventBus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Getter of asyncEventBus.
     *
     * @return asyncEventBus
     */
    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    /**
     * Getter of mailEventBus.
     *
     * @return mailEventBus
     */
    public EventBus getMailEventBus() {
        return mailEventBus;
    }

    /**
     * Getter of importEventBus.
     *
     * @return importEventBus
     */
    public EventBus getImportEventBus() {
        return importEventBus;
    }
}
```

2. **ServiceManager.java**
```java
package com.sismics.reader.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.listener.async.*;
import com.sismics.reader.core.listener.sync.DeadEventListener;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.util.EnvironmentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ServiceManager {
    private FeedService feedService;
    private IndexingService indexingService;

    public ServiceManager() {

        feedService = new FeedService();
        feedService.startAndWait();

        ConfigDao configDao = new ConfigDao();
        Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
        indexingService = new IndexingService(luceneStorageConfig != null ? luceneStorageConfig.getValue() : null);
        indexingService.startAndWait();
    }

    /**
     * Getter of feedService.
     *
     * @return feedService
     */
    public FeedService getFeedService() {
        return feedService;
    }

    /**
     * Getter of indexingService.
     *
     * @return indexingService
     */
    public IndexingService getIndexingService() {
        return indexingService;
    }


}
```

3. **AppContext.java**
```java
package com.sismics.reader.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.reader.core.constant.ConfigType;
import com.sismics.reader.core.dao.jpa.ConfigDao;
import com.sismics.reader.core.listener.async.*;
import com.sismics.reader.core.listener.sync.DeadEventListener;
import com.sismics.reader.core.model.jpa.Config;
import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.service.IndexingService;
import com.sismics.util.EnvironmentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Global application context.
 *
 * @author jtremeaux
 */
public class AppContext {
    /**
     * Singleton instance.
     */
    private static AppContext instance;

    private ServiceManager serviceManager;

    private EventBusManager eventBusManager;

        /**
     * Private constructor.
     */
    private AppContext() {
        serviceManager = new ServiceManager();
        eventBusManager = new EventBusManager();
    }

    /**
     * Returns a single instance of the application context.
     * 
     * @return Application context
     */
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }
    public EventBusManager getEventBusManager() {
        return eventBusManager;
    }   
}
```

**LLM Suggestions:**

Prompt:
```bash
How to remove this design smell(God Object) for AppContext?
```

- ChatGPT-4o:
![ChatGPT](llm_responses/smell-8/smell8_1.png)
![ChatGPT](llm_responses/smell-8/smell8_2.png)

**Quality Impact:**
1. Improved Maintainability: By breaking down a God Object into smaller, focused classes, the code becomes easier to understand and maintain. Each class can be modified independently.
2. Enhanced Testability: Smaller classes with single responsibilities are easier to test. Unit tests can be written for each class in isolation, leading to more reliable tests.
3. Better Code Reusability: When classes are focused on specific tasks, they can often be reused in different contexts without modification.
4. Increased Flexibility: With reduced coupling, changes in one part of the system are less likely to impact other parts, making the system more adaptable to change.
5. Clearer Design: Refactoring God Objects leads to a clearer design, making it easier for new developers to understand the system and for existing developers to navigate the codebase.