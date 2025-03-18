# Project - 2

## Feature - 1: User Registration

### Frontend Changes

Added a sign-up button, and a form. On clicking the sign-up button, the data is sent to the backend through the API endpoint *../user* using a **PUT** request. Also, the warning of changing the default password is removed for newly created users.

### Design Patterns Used

#### Facade

In the method `register` in `UserResource.java`, all of the new user creation is taking place. This includes communication with the Database, creation of new `User` objects, handling connection with front-end, validating the user details etc. This is a problem as the Single Responisbility principle is violated. So, Facade pattern is used.

**Rationale:**

Facade is a structural design pattern that provides a simplified interface to a library, a framework, or any other complex set of classes.

This seperates the concerns and abstracts the details required by the method handiling the end-point.

Currently, `register` method handlesonly the connection to front-end.


**Changes**

- A class RegistrationFacade is created, whcih handles all the user creation logic. 
- All the steps like validation, creating the `User` object and updating the database is done by this class using different methods.
- Only one method `registerUser` is exposed by this class, encapsulating all the other details to other classes.
- This greatly improves extensibility of system as it is easy to extend each of these steps by just modifying the method relating to it.
- Testability is also increased as each step can be independently unit tested.


**Refactoring:**
`
```plantuml

class UserResource {
    + register(username: String, password: String, localeId: String, email: String): Response
}

class RegistrationFacade {
    - userDao: UserDao
    - categoryDao: CategoryDao
    - validationChain: Validator

    + RegistrationFacade()
    + registerUser(username: String, password: String, email: String, localeId: String): void
    - createUserInDatabase(user: User): String
    - createRootCategory(userId: String): void
    - notifyUserCreation(user: User): void
}

UserResource o-- RegistrationFacade : creates >


note right of UserResource
  REST endpoint that handles
  user registration requests
end note


note right of RegistrationFacade
  Implements Facade pattern to simplify
  user registration process
end note

@enduml

```


#### Chain of Responsibility


In the validation step, currently various methods are present for validating different attributes (Ex. `validateEmail`, `validatePassword` etc.). All of these are used sequentially one after one whenever there is a need for validation. This hampers extensibility where we want to expand the validation checks.

**Rationale:**

Chain of Responsibility is a behavioral design pattern that lets you pass requests along a chain of handlers. Upon receiving a request, each handler decides either to process the request or to pass it to the next handler in the chain.

This separates the validation logic into individual classes and allows for easy addition or modification of validation rules.

Currently, the validation chain processes username, password, and email validations in sequence.

**Changes**

- Created abstract BaseValidator class implementing the Validator interface to provide common chain functionality
- Created separate validator classes (UsernameValidator, PasswordValidator, EmailValidator) for each validation rule
- Each validator can be configured with a next validator to form the chain
- RegistrationFacade sets up the chain during initialization
- Each validator independently handles its validation logic and passes the request to the next validator
- New validation rules can be easily added by creating new validator classes and inserting them into the chain

**Refactoring:**

```plantuml
interface Validator {
    + validate(request: RegistrationRequest): void
    + setNext(next: Validator): void
}

abstract class BaseValidator {
    # nextValidator: Validator
    + setNext(next: Validator): void
    # validateNext(request: RegistrationRequest): void
}

class UsernameValidator {
    + validate(request: RegistrationRequest): void
}

class PasswordValidator {
    + validate(request: RegistrationRequest): void
}

class EmailValidator {
    + validate(request: RegistrationRequest): void
}

class RegistrationRequest {
    - username: String
    - password: String
    - email: String
    - localeId: String
}

Validator <|.. BaseValidator
BaseValidator <|-- UsernameValidator
BaseValidator <|-- PasswordValidator
BaseValidator <|-- EmailValidator

UsernameValidator --> PasswordValidator : next >
PasswordValidator --> EmailValidator : next >

note right of Validator
  Defines interface for handling
  validation requests
end note

note right of BaseValidator
  Provides common functionality
  for chaining validators
end note

@enduml
```

Currently the following checks are implemented:

- Unique Email
- Email Format
- Password > 8 letters

**Example Usage:**

```java
// Setting up the chain
UsernameValidator usernameValidator = new UsernameValidator();
PasswordValidator passwordValidator = new PasswordValidator();
EmailValidator emailValidator = new EmailValidator();

usernameValidator.setNext(passwordValidator);
passwordValidator.setNext(emailValidator);

// Using the chain
RegistrationRequest request = new RegistrationRequest(username, password, email, localeId);
usernameValidator.validate(request); // Starts the validation chain
```

This pattern makes the validation process more maintainable and extensible while keeping each validation rule focused and independent.


**Complete Refactoring for this feature:**

```plantuml
@startuml RegistrationFacade

class UserResource {
    + register(username: String, password: String, localeId: String, email: String): Response
}

class RegistrationFacade {
    - userDao: UserDao
    - categoryDao: CategoryDao
    - validationChain: Validator

    + RegistrationFacade()
    + registerUser(username: String, password: String, email: String, localeId: String): void
    - createUserInDatabase(user: User): String
    - createRootCategory(userId: String): void
    - notifyUserCreation(user: User): void
}

class UserDao {
    + create(user: User): String
}

class CategoryDao {
    + create(category: Category): void
}

interface Validator {
    + validate(request: RegistrationRequest): void
    + setNext(next: Validator): void
}

class UsernameValidator {
    + validate(request: RegistrationRequest): void
}

class PasswordValidator {
    + validate(request: RegistrationRequest): void
}

class EmailValidator {
    + validate(request: RegistrationRequest): void
}

class RegistrationRequest {
    - username: String
    - password: String
    - email: String
    - localeId: String
    + getters()
    + setters()
}

UserResource o-- RegistrationFacade : creates >
RegistrationFacade --> UserDao
RegistrationFacade --> CategoryDao
RegistrationFacade --> Validator

Validator <|.. UsernameValidator
Validator <|.. PasswordValidator
Validator <|.. EmailValidator

UsernameValidator --> PasswordValidator: next >
PasswordValidator --> EmailValidator: next >

RegistrationFacade ..> RegistrationRequest
RegistrationFacade ..> User

note right of UserResource
  REST endpoint that handles
  user registration requests
end note

note right of RegistrationFacade
  Implements Facade pattern to simplify
  user registration process and
  Chain of Responsibility for validation
end note

@enduml
```



## Feature-2: Filtering Articles

### Backend Changes

The `FilteredArticleResource` class provides an API endpoint to retrieve articles filtered by categories, sources, and other criteria. It uses a combination of strategies to apply these filters effectively.


### Design Patterns Used

#### Strategy Pattern

The `FilteredArticleResource` uses the Strategy design pattern to apply different filtering strategies based on the input parameters. This pattern allows the system to choose the appropriate filtering strategy at runtime, making the code more flexible and easier to extend.

**Rationale:**

The Strategy pattern is a behavioral design pattern that enables selecting an algorithm's behavior at runtime. It defines a family of algorithms, encapsulates each one, and makes them interchangeable.

In `FilteredArticleResource`, different strategies are used to filter articles by categories, sources, or a combination of both. This approach separates the filtering logic from the resource class, adhering to the Single Responsibility Principle.

**Changes:**

- Introduced `ArticleFilterStrategy` interface to define the contract for filtering strategies.
- Implemented multiple strategies: `CategoryFilterStrategy`, `SourceFilterStrategy`, and `CombinedFilterStrategy`.
- The `get` method in `FilteredArticleResource` selects the appropriate strategy based on the input parameters.

**Implementation:**

```plantuml
@startuml FilteredArticleResource

class FilteredArticleResource {
    + get(categoryIds: List<String>, sourceIds: List<String>, filter: String, limit: Integer, afterArticle: String): Response
}

interface ArticleFilterStrategy {
    + applyCriteria(builder: UserArticleCriteriaBuilder): UserArticleCriteria
}

class CategoryFilterStrategy {
    - categoryIds: List<String>
    + applyCriteria(builder: UserArticleCriteriaBuilder): UserArticleCriteria
}

class SourceFilterStrategy {
    - sourceIds: List<String>
    + applyCriteria(builder: UserArticleCriteriaBuilder): UserArticleCriteria
}

class CombinedFilterStrategy {
    - categoryIds: List<String>
    - sourceIds: List<String>
    + applyCriteria(builder: UserArticleCriteriaBuilder): UserArticleCriteria
}

FilteredArticleResource --> ArticleFilterStrategy

ArticleFilterStrategy <|.. CategoryFilterStrategy
ArticleFilterStrategy <|.. SourceFilterStrategy
ArticleFilterStrategy <|.. CombinedFilterStrategy

note right of FilteredArticleResource
  REST endpoint that handles
  filtered article retrieval requests
end note

note right of ArticleFilterStrategy
  Defines interface for filtering
  strategies
end note

note right of CategoryFilterStrategy
  Filters articles by categories
end note

note right of SourceFilterStrategy
  Filters articles by sources
end note

note right of CombinedFilterStrategy
  Filters articles by both categories
  and sources
end note

@enduml
```

This pattern makes the filtering process more maintainable and extensible while keeping each filtering rule focused and independent.

## Feature-3: Bug Reporting
Task - Users will be able to submit issues within the RSS Reader itself - to make things simpler an issue is just a statement (description) with a timestamp. The admin will have access to a bug dashboard where they can view, mark bugs as resolved, or delete irrelevant reports. Users should also be able to delete their reported bugs

### Features Implemented:
- **Users can submit bug reports**
  - Users can fill out a form to describe the encountered bug.
  - Upon submission, the bug report is stored in the database and visible in user-specific logs.
  
- **Users can delete their reports**
  - Users are provided with a delete button next to each of their submitted bug reports.
  
- **Admin can change the status of the report**
  - Admins can update the status of any bug report to one of the following statuses: OPEN, CLOSED, IN PROGRESS, RESOLVED.
  
- **Admin can delete the reports**
  - Admins have the ability to delete any bug report from the system.

### Frontend Changes:
1. **Bug Report Link Update**
   - Updated the "Report a bug" link in the toolbar to point to an internal page (`#/bugsreport/`) rather than an external GitHub link.
   
2. **Bug Reporting Form**
   - Created a dedicated form for submitting new bug reports, featuring fields for description and email with validation.
   
3. **User View of Reports**
   - Added a table view that lists all bug reports made by the logged-in user.
   - Each entry includes options to delete the report.
   
4. **Admin Dashboard Enhancements**
   - Admins can see all bug reports in a comprehensive table.
   - Included dropdown menus to modify the status of each bug report.
   - Provided delete functionality for each bug report.

5. **Status Dropdown**
   - Added a dropdown menu allowing users/admins to filter bug reports based on their status (OPEN, CLOSED, IN PROGRESS, RESOLVED). Feature Not Yet Implemented.

### Design Patterns Used

<!-- #### Builder Pattern

The Builder pattern is used to construct complex objects step by step. It allows for the creation of different representations of an object using the same construction process.

In the `BugReport` class, the Builder pattern is used to create instances of `BugReport` with various attributes.


**Implementation:**
```plantuml
@startuml
class BugReport {
    - id
    - description
    - timestamp
    - status
    - user_id
}
@enduml
``` -->


### Service Layer Pattern

The Service Layer pattern encapsulates business logic and provides a layer of abstraction between the presentation layer and the data access layer. 

#### Implementation Details:
- A BugReportService class has been created to handle operations such as creating, updating, deleting, and retrieving bug reports.
- This layer ensures that all business rules are centralized and not scattered across controllers or DAOs.
     



**Implementation:**
```plantuml
@startuml
class BugReportService {
    +createBugReport(bugReport: BugReport): BugReport
    +updateBugReport(bugReport: BugReport): BugReport
    +deleteBugReport(id: int): void
    +getBugReport(id: int): BugReport
  }
@enduml
```

### Singleton Pattern

Implemented in `BugReportService` to ensure there's a single instance managing bug reports throughout the application lifecycle.

```plantuml
@startuml
class BugReportService {
    - INSTANCE: BugReportService
    --
    + getInstance(): BugReportService
    + createBugReport(email: String, description: String): String
    + updateBugReportStatus(id: String, status: BugStatus): void
    + deleteBugReport(id: String): void
    + getBugReport(id: String): BugReportDto
    + getAllBugReports(): List<BugReportDto>
    + getBugReportsByEmail(email: String): List<BugReportDto>
}
@enduml
```

### Future Scope
- I have added filter to the bug report table. It should be implemented further in the future

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

## Feature-5B: User-Created Feeds

The tasks we have to implement in this feature are:

1. Allow users to create custom feeds by adding articles from the already existing articles into the feed.
2. Users should be able to subscribe the customized the feeds of the other users

### Flow:
- Every article has `Add to My Feeds` button.
- When we click `Add to My Feeds` button, the dialogue box will create with the list of feeds created by that user and text box to create a new feed.
    - we have to give the title of the feed we want to create. Now, we will create the feed with the title we have given and adds the article to that feed.
    - If we select the feed from the list, the article will be added to that feed.

- The user can see the feeds created by him in the `My Feeds` section.

- The user can see the feeds created by the other users in the `Curated Feeds` section.
    - List of feeds created by all users will be displayed.
    - The user can subscribe to the feeds created by other users by clicking `Subscribe` button in it.

- When we subscribe the feed created by the other user, the feed will be added to the `Subscriptions` section.

## Feature-6A: Daily Report - Using LLM

### 1. Tasks Implemented

#### Article Summarization

- The system fetches articles from the database and applies AI-based summarization using an external Python script (`summarizer.py`).<br>

- The `ArticleSummary` and `ReportSummary` classes handle the summarization logic.

- The summarization is influenced by a weighting mechanism based on the category hierarchy.


#### User-Specific Summaries
- The system generates personalized daily reports for users based on their subscribed feeds.
- The daily report is generated for `10` most recent subscribed articles.
- It organizes the latest articles into a structured format for better readability.
- If no new articles exist, the system ensures an empty response instead of returning errors.



#### Weighted Summarization
- Articles from a top-level category receive a higher weight (1.0).
- Articles from subcategories receive a lower weight (0.7) to emphasize higher-level content.
- Higher the weight, the llm's response would be more elaborative.
- The determineCategoryWeight() method (in `ArticleSummaryResource` and `GenerateResource`) applies this logic by analyzing a user’s feed subscriptions.


### 2. Design Pattern Used
#### Strategy Pattern
The Strategy Pattern is used to separate the summarization logic from the main application logic.

Rationale : 
- Encapsulation of Summarization Logic: <br>
The summarization process is abstracted into the SummarizationStrategy interface.
Concrete implementations (ArticleSummary and ReportSummary) handle different summarization tasks.

- Flexibility & Extensibility : <br>
New summarization techniques (e.g., different AI models, external APIs) can be introduced by simply creating a new implementation of SummarizationStrategy.
The system can dynamically switch between different summarization methods (e.g., short summaries for articles, detailed summaries for reports).


Implementation : 

```
@startuml

interface SummarizationStrategy {
    + summarize(text: String, weight: double): String
    + callPythonSummarizer(text: String, weight: double, promptType: int): String
}

class ArticleSummary {
    + summarize(text: String, weight: double): String
}

class ReportSummary {
    + summarize(text: String, weight: double): String
}

class ArticleSummaryResource {
    - summarizationStrategy: SummarizationStrategy
    + get(unread: boolean, limit: Integer, afterArticle: String): Response
}

class GenerateResource {
    - articlesummary: SummarizationStrategy
    - reportsummary: SummarizationStrategy
    + get(unread: boolean, limit: Integer, afterArticle: String): Response
}

SummarizationStrategy <|.. ArticleSummary
SummarizationStrategy <|.. ReportSummary

ArticleSummaryResource --> SummarizationStrategy
GenerateResource --> SummarizationStrategy

@enduml

```


## Feature-6B: - Duplicate Detection

### 1. Tasks Implemented
####  Article Similarity Detection
- The system identifies duplicate articles based on Named Entity Recognition (NER) and semantic similarity.
- It uses spaCy for extracting named entities (e.g., organizations, people, and locations).
- Cosine similarity is applied to article embeddings for semantic comparison.
- The final similarity score is computed using a weighted sum:
    - 40% weight to named entity overlap.
    - 60% weight to semantic similarity.

#### Duplicate Article Filtering
- If two articles exceed a similarity threshold (default 0.8), they are marked as duplicates.
- The system filters out duplicate articles, ensuring users receive only unique content.



### 2. Design Pattern Used
#### Command Pattern 
- Encapsulates duplicate detection logic in a separate command object (DetectorCommand).
- Allows dynamic execution of commands (e.g., using PythonDetectorCommand for Python-based detection).
- Decouples request sender (DetectorResource) from the actual detection logic, making the system more flexible.

````
@startuml

interface DetectorCommand {
    + execute(articleIds: List<String>, titles: List<String>, descriptions: List<String>, threshold: double): String
}

class PythonDetectorCommand {
    + execute(articleIds: List<String>, titles: List<String>, descriptions: List<String>, threshold: double): String
}

class DetectorResource {
    - detectorCommand: DetectorCommand
    + get(unread: boolean, limit: Integer, afterArticle: String): Response
}

DetectorCommand <|.. PythonDetectorCommand
DetectorResource --> DetectorCommand

@enduml

````
