## Initial Readings

Obtained from CodeMR
Overall Stats
Total lines of code: 10211
Number of classes: 232
Number of packages: 58
Number of external packages: 89
Number of external classes: 340
Number of problematic classes: 8
Number of highly problematic classes: 0

## **Chosen Metrics for Code Quality Assessment**

### **1. McCabe Cyclomatic Complexity**
Cyclomatic complexity assesses the complexity of a method by counting the number of decision points (e.g., `if`, `for`, `while`, `case`) in a method, plus one for the method entry. High cyclomatic complexity indicates more potential execution paths, making the code harder to maintain and test.

**Formula:**
\[ CC = E - N + 2P \]
where:
- \( E \) = Number of edges in the control flow graph
- \( N \) = Number of nodes in the control flow graph
- \( P \) = Number of connected components (usually 1 for a single method)

**Design Smells Impacting This Metric:**
- **Cyclic Dependency**: Increases complexity as interdependent services introduce multiple paths.
- **Insufficient Modularization**: Leads to large methods with multiple responsibilities, increasing decision points.
- **Deficient Encapsulation**: Directly exposing internal data increases branching logic for handling multiple states.

---

### **2. Lines of Code (LOC)**
Measures the number of lines in a codebase, which helps estimate maintainability, readability, and complexity. High LOC often indicates bloated classes or methods.

**Formula:**
\[ LOC = \sum_{i=1}^{n} LoC_{i} \]
where \( LoC_{i} \) is the number of lines in each file/method.

**Design Smells Impacting This Metric:**
- **Broken Modularization**: Large constant files add unnecessary LOC.
- **Wide Hierarchy**: Many direct subclasses can increase LOC through repeated or unnecessary code.
- **Insufficient Modularization**: Bloated DTOs lead to excessive getter/setter methods, increasing LOC.

---

### **3. Number of Children (NOC)**
Represents the number of immediate subclasses of a class. A high NOC value can indicate a deep hierarchy, which affects maintainability.

**Formula:**
\[ NOC = \text{Number of immediate subclasses} \]

**Design Smells Impacting This Metric:**
- **Wide Hierarchy**: Too many direct subclasses of `BaseResource` increase NOC, making the system harder to refactor.
- **Unutilized Abstraction**: If inheritance is used inefficiently, it can lead to unnecessary subclassing.

---

### **4. Weighted Method Count (WMC)**
The sum of the complexities of all methods in a class. A high WMC suggests a class is doing too much, violating the Single Responsibility Principle (SRP).

**Formula:**
\[ WMC = \sum_{i=1}^{n} CC_{i} \]
where \( CC_{i} \) is the cyclomatic complexity of method \( i \).

**Design Smells Impacting This Metric:**
- **Deficient Encapsulation**: Public fields and large data exposure increase method counts due to additional validation logic.
- **Cyclic Dependency**: Leads to classes with a high number of interconnected methods.
- **Insufficient Modularization**: Bloated DTOs result in unnecessary methods, increasing WMC.

---

### **5. Efferent Coupling (Ce)**
Measures the number of classes that a given class depends on. High efferent coupling indicates tight coupling, reducing modularity and reusability.

**Formula:**
\[ Ce = \text{Number of external classes used} \]

**Design Smells Impacting This Metric:**
- **Cyclic Dependency**: Creates excessive coupling between services and DAOs, increasing Ce.
- **Unutilized Abstraction**: Lack of proper inheritance increases direct dependencies, raising efferent coupling.
- **Broken Modularization**: Poorly structured constants lead to unnecessary dependencies across modules.

---

### **6. Lack of Cohesion Across Methods (LCOM)**
Measures the degree to which methods in a class operate on the same set of instance variables. High LCOM indicates a class is trying to do too many things, violating SRP.

**Formula:**
\[ LCOM = \frac{|M| - |I|}{|M| - 1} \]
where:
- \( M \) = Number of methods in a class
- \( I \) = Number of method pairs sharing at least one field

**Design Smells Impacting This Metric:**
- **Broken Modularization**: Unrelated constants force methods to interact with different concerns, increasing LCOM.
- **Deficient Encapsulation**: Exposed internal structures result in methods interacting with unrelated parts of the class.
- **Insufficient Modularization**: Large DTOs introduce mixed responsibilities, reducing cohesion.

---

### **7. Instability**
Measures the ratio of efferent coupling (outgoing dependencies) to total coupling (incoming + outgoing). Higher instability suggests a module is highly dependent on others and prone to changes.

**Formula:**
\[ I = \frac{Ce}{Ce + Ca} \]
where:
- \( Ce \) = Efferent Coupling (number of classes a module depends on)
- \( Ca \) = Afferent Coupling (number of classes depending on this module)

**Design Smells Impacting This Metric:**
- **Cyclic Dependency**: Services relying on each other create an unstable system with high dependency.
- **Insufficient Modularization**: Large DTOs with cross-domain data make the system fragile to changes.
- **Wide Hierarchy**: Deep inheritance trees increase dependency and instability.

---

| QualifiedName | Name | Complexity | Coupling | Size | Lack of Cohesion | CBO | NOC | LOC | LCAM | EC | Ins | WMC | MCC |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| com.sismics.reader.rest.resource | com.sismics.reader.rest.resource | low-medium | medium-high | medium-high | low |  |  | 1333 |  | 13 | 1.0 | 241 |  |
| com.sismics.reader.rest.resource.AllResource | AllResource | medium-high | medium-high | low-medium | low | 13 | 0 | 51 | 0.375 |  |  | 8 |  |
| com.sismics.reader.rest.resource.AppResource | AppResource | low-medium | medium-high | low-medium | low | 16 | 0 | 75 | 0.5 |  |  | 12 |  |
| com.sismics.reader.rest.resource.ArticleResource | ArticleResource | low-medium | medium-high | low-medium | low | 12 | 0 | 100 | 0.333 |  |  | 22 |  |
| com.sismics.reader.rest.resource.BaseResource | BaseResource | low | low | low | low | 5 | 11 | 21 | 0.167 |  |  | 8 |  |
| com.sismics.reader.rest.resource.CategoryResource | CategoryResource | high | medium-high | low-medium | low | 17 | 0 | 160 | 0.5 |  |  | 26 |  |
| com.sismics.reader.rest.resource.JobResource | JobResource | low-medium | low-medium | low | low | 6 | 0 | 19 | 0.0 |  |  | 5 |  |
| com.sismics.reader.rest.resource.LocaleResource | LocaleResource | low-medium | low | low | low | 3 | 0 | 15 | 0.0 |  |  | 2 |  |
| com.sismics.reader.rest.resource.SearchResource | SearchResource | medium-high | low-medium | low | low | 10 | 0 | 25 | 0.0 |  |  | 4 |  |
| com.sismics.reader.rest.resource.StarredResource | StarredResource | medium-high | medium-high | low-medium | low | 11 | 0 | 102 | 0.45 |  |  | 21 |  |
| com.sismics.reader.rest.resource.SubscriptionResource | SubscriptionResource | very-high | very-high | medium-high | low | 38 | 0 | 365 | 0.56 |  |  | 55 |  |
| com.sismics.reader.rest.resource.TextPlainMessageBodyWriter | TextPlainMessageBodyWriter | low | low | low | low | 3 | 0 | 24 | 0.208 |  |  | 4 |  |
| com.sismics.reader.rest.resource.ThemeResource | ThemeResource | low-medium | low | low | low | 4 | 0 | 19 | 0.0 |  |  | 4 |  |
| com.sismics.reader.rest.resource.UserResource | UserResource | very-high | very-high | medium-high | low | 37 | 0 | 357 | 0.582 |  |  | 70 |  |
| com.sismics.reader.core.dao.jpa | com.sismics.reader.core.dao.jpa | low | medium-high | medium-high | low |  |  | 680 |  | 13 | 0.419 | 132 |  |
| com.sismics.reader.core.dao.jpa.ArticleDao | ArticleDao | low-medium | low-medium | low-medium | low | 8 | 0 | 96 | 0.6 |  |  | 15 |  |
| com.sismics.reader.core.dao.jpa.FeedSubscriptionDao | FeedSubscriptionDao | low-medium | low-medium | low-medium | low-medium | 8 | 0 | 93 | 0.667 |  |  | 20 |  |
| com.sismics.reader.core.dao.jpa.UserArticleDao | UserArticleDao | low-medium | low-medium | low-medium | low | 7 | 0 | 118 | 0.567 |  |  | 32 |  |
| com.sismics.reader.core.constant | com.sismics.reader.core.constant | low | low | low | low |  |  | 17 |  | 0 | 0.0 | 0 |  |
| com.sismics.reader.core.dao.file.html.FaviconDownloader | FaviconDownloader | low | low | low-medium | low | 5 | 0 | 80 | 0.0 |  |  | 14 |  |
| com.sismics.reader.core.dao.file.rss.RssReader | RssReader | very-high | medium-high | medium-high | high | 18 | 0 | 362 | 0.804 |  |  | 217 |  |
| com.sismics.reader.core.service | com.sismics.reader.core.service | low | low | low-medium | low |  |  | 360 |  | 2 | 0.222 | 97 |  |
| com.sismics.reader.core.service.FeedService | FeedService | very-high | very-high | low-medium | medium-high | 44 | 0 | 268 | 0.759 |  |  | 70 |  |
| com.sismics.reader.core.service.IndexingService | IndexingService | high | high | low-medium | low | 22 | 0 | 92 | 0.556 |  |  | 27 |  |