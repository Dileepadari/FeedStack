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


### 2. Cyclic Dependency
**Location:** 
- `com.sismics.reader.core.service.FeedService`
- `com.sismics.reader.core.service.IndexService`

**Problem:**
Multiple circular dependencies:
- FeedService ↔️ FeedDao
- FeedService ↔️ AppContext
- FeedService ↔️ FeedSubscription

**Solution:**
1. Implement Dependency Injection
2. Use Factory pattern for service instantiation
3. Introduce service interfaces
4. Extract FeedSyncService for synchronization
5. Update AppContext to use factories

**Quality Impact:**
- Maintainability: Changes cascade through dependent components
- Testability: Complex mocking required
- Reusability: Components tightly coupled
- Flexibility: Changes have system-wide effects

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
- Mixed responsibilities in DTOs
- Tight coupling between concerns
- Code duplication across DTOs

**Solution:**
1. Split DTOs by domain concept
2. Create composite DTOs
3. Implement Builder pattern
4. Update mappers and service layer

**Quality Impact:**
- Maintainability: Mixed concerns make changes difficult
- Reusability: DTOs too specific to reuse
- Testability: Complex objects hard to test
- Flexibility: Changes affect multiple components

### 5. Unutilized Abstraction
**Location:** `com.sismics.reader.rest.resource.*`

**Problem:**
- Ineffective use of BaseResource inheritance
- Duplicated logic across resources
- Repeated authentication and error handling

**Solution:**
1. Extract common logic to base class
2. Implement proper inheritance
3. Standardize error handling

**Quality Impact:**
- Maintainability: Duplicated code increases maintenance burden
- Reusability: Common functionality not shared
- Complexity: Duplicate patterns increase cognitive load

### 6. Wide Hierarchy
**Location:** `com.sismics.reader.rest.resource.BaseResource`

**Problem:**
- Too many direct subclasses
- Missing intermediate abstractions
- Poor organization of endpoints

**Solution:**
1. Create intermediate abstract classes
2. Group related functionality
3. Reorganize inheritance hierarchy

**Quality Impact:**
- Maintainability: Inconsistent patterns
- Reliability: Inconsistent error handling
- Security: Varying authentication implementations


All of these design smells are identified using DesigniteJava. It identified many more, but these were selected.