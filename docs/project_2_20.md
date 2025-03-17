## Feature-4: Making Categories Better 
Task - Enhance the category system to support nesting, allowing up to 5 levels of subcategories. Each category and subcategory should display metadata such as the number of unread items and the total count of articles. The UI should render nested categories in a collapsible format, allowing categories to host both individual feeds and subcategories simultaneously.
### Features Implemented
1. Hierarchical Structure:

- Categories can contain both subscriptions and subcategories
- Support for up to 5 levels of nesting
- Proper parent-child relationships maintained


2. Metadata Display:

- Unread counts calculated and displayed for each category
- Total article counts aggregated through the hierarchy
- Visual distinction between unread and total counts


3. Interactive UI:

- Collapsible/expandable categories
- Drag-and-drop reorganization of categories and subscriptions
- Persistent state across sessions


### Design Patterns Used:
1. **Composite Pattern**
The Composite Design Pattern is a structural design pattern that lets you compose objects into tree-like structures to represent part-whole hierarchies. It allows clients to treat individual objects and compositions of objects uniformly. In other words, whether dealing with a single object or a group of objects (composite), clients can use them interchangeably.

#### Rationale:
The Composite pattern is a structural design pattern that lets you compose objects into tree structures to represent part-whole hierarchies. In our RSS reader, we needed to represent a hierarchical structure of categories and subscriptions, where categories can contain both subscriptions (leaf nodes) and other categories (composite nodes).
This pattern provides an elegant solution for treating individual objects and compositions of objects uniformly. For our nested categories implementation, this meant we could handle operations on both simple subscriptions and complex nested category structures using the same interface.The most compelling evidence of the Composite pattern is in our recursive implementations. The recursive approach elegantly handles the tree structure without needing to know the depth or complexity of the hierarchy.

Categories function as the composite nodes in our tree. The dual containment capability is the essence of the Composite pattern and is implemented through the children and subscriptions lists in our Category class.
Subscriptions represent the leaf nodes in our hierarchy. They have no children and serve as the endpoints of our tree structure. In our implementation, each subscription:
- Contains data from a single RSS feed
- Maintains its own count of unread and total articles
- Can be moved between different categories
#### Changes:
- Modified the Category class to support parent-child relationships between categories
- Implemented a tree structure in both backend and frontend to represent the hierarchy
- Created recursive functions to process and display the nested structure
- Added depth calculation and restrictions to respect the 5-level nesting limit
- Added calculation of unread and total counts for each category in the hierarchy
- Implemented propagation of counts from child categories to parent categories
- Created a mechanism to update these counts when articles are read or new articles are added

#### Implementation:

```plantuml
@startuml
class Category {
  -id: String
  -name: String
  -parentId: String
  -folded: boolean
  -order: int
  -userId: String
  -children: List<Category>
  +getChildren(): List<Category>
  +setChildren(children: List<Category>): void
}

class FeedSubscription {
  -id: String
  -title: String
  -url: String
  -categoryId: String
  -unreadCount: int
  -totalCount: int
}

class CategoryDao {
  +buildCategoryTree(rootId: String, userId: String): List<Category>
  +computeDepth(category: Category, userId: String): int
}

class CategoryResource {
  +list(): Response
  +buildCategoryJson(category: Category): JSONObject
  +update(id: String, name: String, order: Integer, folded: Boolean, parentId: String): Response
}

Category "1" *-- "many" Category: contains
Category "1" *-- "many" FeedSubscription: contains
CategoryDao --> Category: manages
CategoryResource --> CategoryDao: uses
@enduml
```

In the backend, the Category entity was enhanced to maintain parent-child relationships, and the CategoryDao and the Category Dto was extended to build and manage category trees. The CategoryResource provides REST endpoints to interact with the nested structure.
In the frontend, the UI components were modified to render the hierarchical structure in a collapsible format, allowing users to expand and collapse categories as needed.

#### System Architecture
The nested category implementation follows a client-server architecture:

1. Backend (Java):

- CategoryDao: Manages category persistence and tree construction
- CategoryResource: Provides RESTful endpoints for category operations
- SubscriptionResource: Handles subscription management within categories


2. Frontend (JavaScript):

- r.subscription.js: Manages the rendering and interaction with the category tree
- Event handlers for user interactions (collapse/expand, drag-drop, etc.)





## Feature-5A: Simulating Rss Feeds

The tasks we have to implement in this feature are:

1. Take the Url and check if it is a valid RSS feed.
2. If not valid get content from the page using NewsAPI.
3. Convert that content into RSS feed.
4. API calls should be optimized to minimize request overhead.

### Design Patterns Used:
1. **Strategy Pattern**

#### Rationale:
The Strategy pattern is a behavioral design pattern that enables selecting an algorithm's behavior at runtime. It defines a family of algorithms, encapsulates each one, and makes them interchangeable. Strategy lets the algorithm vary independently from the clients that use it.

In `FeedService` we have to get RssReader object which contains feed data by parsing the Url. We have two algorithms to get the RssReader object. So, we used the Strategy pattern to implement the different strategies for fetching the content from the URL.

#### Changes:
- In this we implement the different strategies for fetching the content from the URL. We have two strategies:
    - `RssUrlStrategy` : This strategy is to parse the rssfeeds from the url which supports RSS feed which returns the RssReader object.
    - `ContentUrlStartegy` : This strategy is used to fetch the content from the URL if it is not a valid RSS feed which returns the RssReader object.
    - `UrlStrategy` : This is the interface which is implemented by the above two strategies.
- We will pick pattern based on the URL for thata i have written a function to check the URL is RSS feed or not and creates the object of repective strategy.

#### Implementation:
```plantuml
@startuml
class FeedService {
    + synchronize(String url): RssReader
    - getUrlStrategy(String url): UrlStrategy
    - checkUrl(String url): int
}

interface UrlStrategy {
    + getRssFeedReader(url: String): RssReader
}

class RssUrlStrategy {
    + getRssFeedReader(url: String): RssReader
}

class ContentUrlStrategy {
    + getRssFeedReader(url: String): RssReader
}

FeedService *-- UrlStrategy
UrlStrategy <|.. RssUrlStrategy
UrlStrategy <|.. ContentUrlStrategy
@enduml
```

This pattern makes the code more modular and easy to maintain. If we want to add more strategies in the future, we can easily add them without changing the existing code.

2. **Adapter Pattern**

#### Rationale:
The Adapter pattern is a structural design pattern that allows objects with incompatible interfaces to collaborate. It acts as a bridge between two incompatible interfaces. This pattern involves a single class called adapter that is responsible for joining functionalities of independent or incompatible interfaces.

In `ContentUrlStrategy` we have to get the content from the URL using NewsAPI. We have to convert the content into RSS feed. So, we used the Adapter pattern to convert the content into RSS feed.

#### Changes:
- `ContentUrlStrategy` class is used to fetch the content from the URL if it is not a valid RSS feed.

- `ContentInterface` is the interface which is implemented by the `FeedAdapter` class.

- `ApiFeedService` is the class which is used to get the content from the URL using NewsAPI.

- `FeedAdapter` class is used to convert the content into RSS feed.

#### Implementation:
```plantuml
@startuml
interface ContentInterface {
    + fetchContent(url: String): InputStream
}

class ApiFeedService {
    + fetchContent(url: String): Json<Object>
}

class FeedAdapter {
    - apiFeedService: ApiFeedService
    + FeedAdapter(apiFeedService: ApiFeedService)
    - convertToRss(content: Json<Object>): InputStream
    + fetchContent(url: String): InputStream
}

class ContentUrlStrategy {
    - apiFeedService: ApiFeedService
    - feedAdapter: FeedAdapter(apiFeedService: ApiFeedService)
    + getRssFeed(url: String): RssReader
}

ContentInterface <|.. FeedAdapter
FeedAdapter *-- ApiFeedService
ContentUrlStrategy --> ContentInterface
@enduml
```
This pattern makes that single adapter can be used to for many different type of adaptees which takes Json and converts it into RssfeedFormat.

### Efficient API Calls:
- We have used the `Cache` pattern to store the data in the cache. So, if the same URL is requested again, we can get the data from the cache instead of making the API call again.

- I used Time-Based Expiration (TTL - Time to Live) for the cache. So, the cache will be valid for a certain amount of time. After that time, the cache will be invalidated and the data will be fetched from the API.

#### Changes:
- `ApiCache` class is used to store the data in the cache.
- `CacheEntry` class is used to store the data in the cache with the time to live.
- `ApiFeedService` class is used to get the content from the URL using NewsAPI. It checks if the data is present in the cache. If the data is present in the cache, it will return the data from the cache. If the data is not present in the cache, it will make the API call and store the data in the cache.

```java
private ApiCache cache = new ApiCache(Duration.ofMinutes(15));

private ApiFeedService() {
}

public static ApiFeedService getInstance() {
    if (instance == null) {
        instance = new ApiFeedService();
    }
    return instance;
    }

public Optional<JSONObject> fetchContent(String apiCall) {
   
  Optional<JSONObject> cachedContent = cache.get(apiCall);
   if (cachedContent.isPresent()) {
        logger.info("Returning cached content for: {}", apiCall);
      return cachedContent;
   }

   try {
        logger.info("Fetching content from: {}", apiCall);
        URL url = new URL(apiCall);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                cache.put(apiCall, Optional.of(new JSONObject(response.toString())));
                return Optional.of(new JSONObject(response.toString()));
            }
        } else {
            logger.error("Failed to fetch content. HTTP Response Code: {}", responseCode);
        }
    } catch (Exception e) {
        logger.error("Exception while fetching content: ", e);
    }
    cache.put(apiCall, Optional.empty());
    return Optional.empty();
}
```

#### llm Responses:
I asked llm what are the different techniques to optimize the API calls. It suggested me to use the `Caching`, `Batching`, `Reduce Frequency of Requests`.
![Feature-5A](llm_reponses/feature_5A/1.png)
![Feature-5A](llm_reponses/feature_5A/2.png)
![Feature-5A](llm_reponses/feature_5A/3.png)
![Feature-5A](llm_reponses/feature_5A/4.png)
![Feature-5A](llm_reponses/feature_5A/5.png)
![Feature-5A](llm_reponses/feature_5A/6.png)
![Feature-5A](llm_reponses/feature_5A/7.png)
![Feature-5A](llm_reponses/feature_5A/8.png)


- We can also use `Conditional Requests` to optimize the API calls. We can use the `ETag` and `Last-Modified` headers to check if the data has been modified since the last request. If the data has not been modified, we can return the `304 Not Modified` response.

But NewsApi does not support the `ETag` and `Last-Modified` headers. So, we cannot use the `Conditional Requests` technique.

- Batch Requesting is better in this case than caching but NewsApi does not support the Batch Requesting. So, we cannot use the `Batch Requesting` technique.





