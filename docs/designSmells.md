## Broken Modularization - com.sismics.reader.core.constant


**Problem:**

1. Many constants not related to each other are in the same class.

Violates Single Responsibility Principle.

**Solution:**

1. Separate constants into different classes.

2. If some constants are related to each other, put them in the same class.


**Quality Attributes Affected:**

1. Maintainability

Changes in one concern (e.g., job-related constants) affect an unrelated area (e.g., localization).
The file grows large and difficult to manage.

2. Scalability 

Adding new features or constants requires modifying this single class, making it harder to scale.

3. Readability 

Mixing unrelated concerns reduces clarity and makes it difficult for developers to locate relevant constants.

4. Reusability

Modules cannot reuse only the constants they need, leading to unnecessary dependencies.

## Cyclic Dependency - com.sismics.reader.core.service.FeedService, com.sismics.reader.core.service.IndexService


**Problem:**

Cyclic dependency between classes.

FeedService -> FeedDao -> FeedService

This is happening as new DAOs re being directly created in the service layer.

So tight coupling between service and dao layers.

FeedService -> AppContext -> FeedService

FeedService is created and managed by AppContext. FeedService is also using AppContext to get other services.

FeedService -> FeedSubscription -> FeedService

FeedService manages subscriptions. FeedSubscription requires FeedService for synchronization.

**Solution:**

1. Use Dependency Injection to manage dependencies.

2. Use Factory pattern to create instances of services.


- Introduction of interfaces for services. (IFeedService, IFeedRepository)

- Extract Service Classes.
(FeedSyncService is a new class that is responsible for synchronization of feeds.)

- Factory Classes. (FeedFactory)

- Update AppContext to use factory classes.

Remove direct instantiation of FeedService.
Use dependency injection container
Configure component lifecycle management
**Quality Attributes Affected:**

1. Maintainability

Changes in one component require changes in dependent components.Hard to modify components independently. Increased risk of breaking changes

2. Testability

Difficult to unit test components in isolation. Need complex mocking setups. Hard to create test scenarios.

3. Reusability

Components cannot be reused independently. High coupling makes it hard to extract functionality.

4. Flexibility

Difficult to replace implementations. Changes have ripple effects across the system.




## Deficient Encapsulation - com.sismics.reader.core.dao.file.rss.RssReader, com.sismics.reader.core.dao.file.html.FaviconDownloader, com.sismics.reader.core.constant

**Problem:**

The RssReader class exposes internal implementation details and data structures directly, violating encapsulation principles. This happens through:

1. Public fields that expose internal state
2. Methods returning internal data structures without defensive copying
3. Lack of proper access modifiers for class members

Public static DateFormatters, getArticleList returns direct List. 

**Solution:**

1. Make instance variables private and provide controlled access through methods
2. Implement defensive copying for returned collections and mutable objects
3. Create immutable value objects for returned data where appropriate

**Quality Attributes Affected:**

1. Maintainability
   - Changes to internal implementation can affect multiple external components
   - Difficult to modify internal data structures without breaking client code

2. Security
   - External code can directly manipulate internal state
   - Potential for object state corruption


## Insufficient Modularization - com.sismics.reader.core.dao.jpa.dto.UserArticleDto, com.sismics.reader.core.dao.jpa.dto.ArticleDto, com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto

**Problem**

1. These DTOs contain mixed responsibilities by combining data from multiple domain entities
2. They have tight coupling between different concerns (e.g., UserArticleDto combines User, Article, and Feed data)
3. Large number of fields and getter/setter methods make the classes unwieldy
4. Data duplication across DTOs (e.g., article fields repeated in UserArticleDto and ArticleDto)

**Solution**

1. Split DTOs by Domain Concept
2. Create Composite DTOs When Needed
3. Implement Builder Pattern
4. Update Mappers
5. Update Service Layer
   - Modify services to use appropriate DTOs for each operation
   - Create facade services for operations requiring combined data

**Quality Attributes Affected:**

1. Maintainability
   - Improved separation of concerns
   - Reduced code duplication
   - Easier to manage and extend DTOs

2. Reusability
   - DTOs can be reused across different parts of the application

3. Testability
   - Improved testability of DTOs
   - Reduced code duplication
   - Easier to create test scenarios

4. Flexibility
   - Improved flexibility of DTOs
   - Reduced code duplication

## Unutilized Abstraction - All files in com.sismics.reader.rest.resource

**Problem:**

1. Resource classes are designed with inheritance from BaseResource but don't effectively utilize the inheritance relationship
2. Many methods duplicate logic that could be shared through the base class
3. Authentication checks and error handling patterns are repeated across resources

**Solution:**

1. Extract shared logic into a base class
2. Use inheritance for common functionality
3. Update resource classes to inherit from the base class

**Quality Attributes Affected:**

1. Maintainability
   - Code duplication makes changes harder

2. Reusability
   - Common functionality isn't properly abstracted

3. Complexity
   - Duplicate code increases cognitive load


## Wide Heirarchy - com.sismics.reader.rest.resource.BaseResource

**Problem:**

1. Many direct subclasses of BaseResource
2. Lack of intermediate abstractions to group related functionality
3. Poor organization of REST endpoint responsibilities

**Solution:**

1. Create intermediate abstractions
2. Group related functionality in new resource classes
3. Update resource classes to inherit from the new classes

**Quality Attributes Affected:**

1. Maintainability
   - Inconsistent patterns make maintenance difficult

2. Reliability
   - Inconsistent error handling can lead to bugs

3. Security
   - Inconsistent authentication checks create vulnerabilities

