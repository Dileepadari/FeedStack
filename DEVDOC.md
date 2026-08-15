# FeedStack - Developer Documentation

Technical reference for the FeedStack codebase: module layout, architecture, auth model, data model, API surface, and setup. For what the app does from a user's point of view, see [README.md](./README.md).

## Table of contents

- [Tech stack](#tech-stack)
- [Repository layout](#repository-layout)
- [Architecture overview](#architecture-overview)
- [Feed synchronisation](#feed-synchronisation)
- [Auth model](#auth-model)
- [API surface](#api-surface)
- [Data model](#data-model)
- [Data directory and configuration](#data-directory-and-configuration)
- [Local setup](#local-setup)
- [Building distributions](#building-distributions)
- [Branch map](#branch-map)
- [The design smell work](#the-design-smell-work)
- [Automated refactoring pipeline](#automated-refactoring-pipeline)
- [Gotchas](#gotchas)

## Tech stack

Java 8 throughout, compiled by Maven 3 with `maven.compiler.source` and `target` both pinned to `1.8`. The REST layer is Jersey 1.x (JAX-RS), persistence is Hibernate with JPA entities, and the default database is an embedded HSQLDB file, with PostgreSQL supported as an alternative through the same Hibernate configuration. Full text search is Apache Lucene, maintained in a directory on disk beside the database. Jetty serves the app, both under `mvn jetty:run` in development and inside the packaged distributions. The web front end is jQuery plus Backbone with LESS stylesheets, served straight out of `reader-web/src/main/webapp`. The Android client is a separate Gradle project that talks to the same REST API.

Java 8 is a hard requirement, not a preference. Jersey 1.x and the Hibernate version in `pom.xml` do not run on modern JDKs.

## Repository layout

| Module | What it owns |
|---|---|
| `reader-core` | Domain model, JPA entities and DAOs, the feed synchronisation service, the Lucene indexing service, RSS and Atom parsing, the async event listeners |
| `reader-web-common` | Servlet filters shared by the web app, notably the token security filter |
| `reader-web` | Jersey REST resources, the web front end under `src/main/webapp`, and the Jetty run configuration |
| `reader-agent` | Tray agent for the desktop distributions |
| `reader-android` | Android client, built with Gradle rather than Maven |
| `reader-distribution-*` | Packaging for standalone, Debian, RedHat, macOS, Windows and Docker |
| `docs` | Coursework write-ups, UML, metrics, the LLM pipeline and its transcripts |

Only `reader-core`, `reader-web-common` and `reader-web` are in the default Maven reactor. The agent and the distribution modules are behind the `prod` profile, so a plain `mvn install` skips them.

## Architecture overview

```
  Browser (jQuery/Backbone)        Android app
            |                           |
            +------------+--------------+
                         |
              HTTP + auth_token cookie
                         |
                 +---------------+
                 |  reader-web   |   Jersey resources under /api
                 +---------------+
                         |
        +----------------+-----------------+
        |                                  |
+---------------+                 +-----------------+
|  FeedService  |                 | IndexingService |
| polls feeds   |                 | Lucene index    |
+---------------+                 +-----------------+
        |                                  |
        +----------------+-----------------+
                         |
                 +---------------+
                 |  reader-core  |   JPA DAOs
                 +---------------+
                         |
              HSQLDB file (or PostgreSQL)
```

`reader-web` owns nothing but request handling: each resource validates input, calls into `reader-core`, and shapes a JSON response. `reader-core` owns the domain. The two long-lived services, `FeedService` and `IndexingService`, are started by the application context and run for the life of the process.

Work that must not block a request is dispatched as a Guava event and picked up by a listener in `reader-core/.../listener/async`: `ArticleCreatedAsyncListener` and its siblings keep the Lucene index in step with the database, `FaviconUpdateRequestedAsyncListener` fetches feed icons, `SubscriptionImportAsyncListener` runs OPML imports, and `RebuildIndexAsyncListener` handles a full reindex triggered from the admin API.

## Feed synchronisation

`FeedService` extends Guava's `AbstractScheduledService` and runs on a fixed delay schedule: it starts immediately and then repeats every 10 minutes (`Scheduler.newFixedDelaySchedule(0, 10, TimeUnit.MINUTES)`). Each pass walks every feed that has at least one subscriber, fetches it, parses it, and inserts the articles nobody has seen yet.

Every run is recorded in `T_FEED_SYNCHRONIZATION` with its outcome and duration, which is what the per-subscription sync indicator in the UI reads. A single subscription can also be synced on demand through `GET /api/subscription/{id}/sync`.

## Auth model

Authentication is a bearer token kept in a cookie, not a servlet session.

`POST /api/user/login` checks the password against the bcrypt hash in `T_USER`, creates a row in `T_AUTHENTICATION_TOKEN`, and returns it as a cookie named `auth_token` scoped to `/`. If the login asked to be remembered the cookie gets a long max age, otherwise it is a session cookie. `POST /api/user/logout` deletes the token row and clears the cookie.

`TokenBasedSecurityFilter` in `reader-web-common` runs on every request. It reads the `auth_token` cookie, looks the token up, and if it resolves to a user it builds a `UserPrincipal` and attaches it to the request. `SecurityFilter` then loads the user's role from `T_ROLE` and expands it into a set of base functions from `T_ROLE_BASE_FUNCTION`, which is what authorisation checks read. Requests with no valid token get an anonymous principal rather than a rejection; individual resources extending `AuthenticatedResource` are what enforce that a real user is present.

Two roles ship by default, `user` and `admin`. Admin-only endpoints check for the `ADMIN` base function.

The default admin password hash is a constant in `SecurityConfig.DEFAULT_ADMIN_PASSWORD`, seeded by the database migrations. The first run wizard prompts for a replacement, and the app keeps warning until it changes.

## API surface

Everything is under `/api`. All of it requires a valid `auth_token` unless marked otherwise.

| Resource | Endpoints |
|---|---|
| `/user` | `PUT /` register, `POST /` update own account, `POST /{username}` update a user (admin), `GET /check_username`, `POST /login`, `POST /logout`, `DELETE /` delete own account, `DELETE /{username}` (admin), `GET /` current user, `GET /{username}`, `GET /list` (admin) |
| `/subscription` | `GET /` the subscription tree, `GET /{id}` articles in a subscription, `GET /{id}/sync`, `PUT /` subscribe, `POST /{id}` rename or recategorise, `GET /{id}/favicon`, `POST /{id}/read`, `DELETE /{id}` unsubscribe, `PUT /import` OPML in, `GET /export` OPML out |
| `/category` | `GET /` list, `GET /{id}` articles in a category, `PUT /` create, `DELETE /{id}`, `POST /{id}` rename or reorder, `POST /{id}/read` |
| `/article` | `POST /{id}/read`, `POST /read` bulk, `POST /{id}/unread`, `POST /unread` bulk |
| `/all` | `GET /` every unread article, `POST /read` mark everything read |
| `/starred` | `GET /` starred articles, `PUT /{id}` star, `DELETE /{id}` unstar, `POST /star` and `POST /unstar` bulk |
| `/search` | `GET /{query}` Lucene search across the user's articles |
| `/theme` | `GET /` available themes |
| `/locale` | `GET /` available locales |
| `/job` | `DELETE /{id}` cancel a running job |
| `/app` | `GET /` app info, `GET /log` (admin), `POST /batch/reindex` (admin), `POST /map_port` UPnP port mapping |

The `project2_20` branch adds `/trending` and an article summary resource on top of these.

## Data model

Fifteen tables, all prefixed `T_`. Timestamps are stored as epoch milliseconds in `Long` columns, or as `Date` where Hibernate maps them directly; either way they are UTC, and the display timezone comes from the user's locale.

| Table | Holds | Key relationships |
|---|---|---|
| `T_USER` | Account, bcrypt password hash, email, locale, role | `roleId` to `T_ROLE`, `localeId` to `T_LOCALE` |
| `T_AUTHENTICATION_TOKEN` | Issued login tokens with creation and last-use timestamps | `userId` to `T_USER` |
| `T_ROLE` | `user` and `admin` | |
| `T_BASE_FUNCTION` | Named permissions, notably `ADMIN` | |
| `T_ROLE_BASE_FUNCTION` | Which permissions a role has | joins `T_ROLE` and `T_BASE_FUNCTION` |
| `T_LOCALE` | Supported interface locales | |
| `T_CONFIG` | Key/value application settings, keyed by `ConfigType` | |
| `T_FEED` | One row per distinct feed URL, shared across all subscribers | |
| `T_FEED_SUBSCRIPTION` | A user's subscription: their title for the feed, its category, unread count | `userId`, `feedId`, `categoryId` |
| `T_CATEGORY` | User's folder tree, with a parent pointer and a folded flag | `userId`, self-referencing `parentId` |
| `T_ARTICLE` | The article itself, deduplicated per feed by GUID | `feedId` to `T_FEED` |
| `T_USER_ARTICLE` | Per-user read and starred state for an article | `userId`, `articleId` |
| `T_FEED_SYNCHRONIZATION` | One row per sync attempt, with success flag and duration | `feedId` |
| `T_JOB` | Long-running jobs such as OPML import | `userId` |
| `T_JOB_EVENT` | Progress events for a job | `jobId` |

`T_FEED` and `T_ARTICLE` are global; `T_FEED_SUBSCRIPTION` and `T_USER_ARTICLE` are the per-user layer over them. Two users subscribed to the same feed share the feed and article rows and each get their own subscription and read state.

Schema changes are versioned SQL under `reader-core/src/main/resources/db/update`, applied in order at startup by `DbOpenHelper`.

## Data directory and configuration

There is no `.env` file. Configuration is JVM system properties, read in `EnvironmentUtil`:

| Property | Effect |
|---|---|
| `reader.home` | Base data directory. Overrides the per-OS default |
| `hibernate.properties` | Path to a properties file, used to point Hibernate at PostgreSQL instead of the bundled HSQLDB |
| `application.log.enabled` | Whether the in-app log viewer collects entries |
| `ssl.trust.all` | Skip certificate validation when fetching feeds. Development only |
| `test` | Set by the test harness; switches the data directory to a temp dir |
| `news.api.key` | NewsAPI key for `ContentUrlStrategy`. Also read from the `NEWS_API_KEY` environment variable. No default: it was hardcoded and has been removed |

When `reader.home` is unset the base directory is `/var/reader` on Unix, `%APPDATA%\Sismics\Reader` on Windows, and `~/Library/Sismics/Reader` on macOS. `DirectoryUtil` then derives `db/`, `lucene/`, `favicon/` and `log/` beneath it. The Docker distribution sets `reader.home` to `/data` through `reader.xml`.

The database file itself is `<reader.home>/db/reader`, opened as `jdbc:hsqldb:file:...` with `hsqldb.write_delay=false`.

## Local setup

Prerequisites: JDK 8 and Maven 3.

```bash
# from the repository root
mvn clean -DskipTests install -e

# then
cd reader-web
mvn jetty:run
```

The app comes up on <http://localhost:8080/reader-web/> with context path `/reader-web`. First run goes through <http://localhost:8080/reader-web/src/#/wizard>, which creates the admin account.

Run the tests with `mvn test`. They use Grizzly and a temporary data directory, so they do not touch your real `reader.home`.

If your machine defaults to a newer JDK, set `JAVA_HOME` for the shell rather than switching it globally:

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

## Building distributions

```bash
# deployable WAR, from reader-web
mvn -Pprod -DskipTests clean install

# native installers and the agent, from the root
mvn -Pprod -DskipTests clean install
```

Docker uses the built WAR plus `reader.xml`:

```bash
docker build -f reader-distribution-docker/Dockerfile -t feedstack .
```

The Android APK is built separately, from `reader-android`, with Gradle and the Android SDK installed and the keystore environment variables from `build.gradle` set:

```bash
./gradlew build
```

The APK lands in `app/build/apk/app-release.apk`.

## Branch map

The full history of both project phases is preserved here, 120 commits across 18 branches.

| Branch | Contains |
|---|---|
| `master` | Default branch, tagged `v2.0.0`. Both phases: the design smell refactoring plus every phase two feature merged in |
| `project2_20` | Phase two as it was developed. Now merged into `master`, kept for history |
| `broken-mod`, `cyclic-dependency`, `insuff-mod-smell`, `broken-heirarchy`, `unutil-abstraction`, `feature-envy`, `god-class` | One design smell fix each, all merged into `master` |
| `refactored-code` | Output branch of the automated LLM refactoring pipeline |
| `user_registration`, `filtering_rss`, `bug_report`, `categories`, `Simulating_rssfeeds`, `Curated-Feeds`, `dailyreport`, `detection` | One phase two feature each, all merged into `project2_20` |

Everything is on `master` now. `v2.0.0` is the merge of both phases, with the Maven and Android versions set to match.

## The design smell work

`docs/designSmells.md` is the write-up. Eight smells were found with SonarQube and DesigniteJava and fixed, each on its own branch:

1. **Broken modularization** - `Constants` held unrelated constants. Split into `DefaultConfig`, `SecurityConfig`, `LuceneConfig` and `ImportJobEvents` under `core.constant`.
2. **Cyclic dependency** - `FeedService`, `AppContext` and `IndexingService` referenced each other in a loop.
3. **Deficient encapsulation** - public fields and over-broad accessors.
4. **Insufficient modularization** - oversized classes doing several jobs.
5. **Broken hierarchy** - `ThemeResource.java`.
6. **Wide hierarchy** - flat inheritance trees that should have had intermediate types.
7. **Feature envy** - `UserResource.java` reaching into another object's data.
8. **God object** - one class holding far too much of the application.

Raw tool output is in `docs/designite/`, before and after metrics are in `docs/Task2B_Code_Metrics/`, and the LLM transcripts that informed each fix are in `docs/llm_responses/`.

## Automated refactoring pipeline

`docs/llm_pipeline/script.py` clones the repository, pairs up Java files, asks Gemini to find design smells and propose refactorings, writes the result to a `refactored-code` branch, and opens a pull request against `master`. `.github/workflows/periodic_refactor.yml` runs it.

The workflow is **manual trigger only**. It force-pushes `refactored-code` and opens pull requests, so the weekly cron is commented out in the workflow file rather than active. Uncomment the `schedule` block if you want it back.

It needs three repository secrets to do anything: `GEMINI_API_KEY`, `USERNAME` (the GitHub account the commits are attributed to), and the `GITHUB_TOKEN` that Actions injects automatically. The script resolves the target repository from `GITHUB_REPOSITORY`, so it follows the repo it runs in rather than a hardcoded slug.

`docs/bonus/bonus.py` is the separate bonus experiment: it runs the same analysis through Hugging Face CodeLlama and Google T5 to compare free models. Its output is `docs/bonus/free_llm_comparison.md`.

## Gotchas

- **Java 8 only.** Jersey 1.x and this Hibernate version fail on newer JDKs. SonarQube, if you run it, wants Java 11, so keep both installed and switch per task.
- **Tests must not run in parallel forks.** Every REST test binds the Grizzly container to the hardcoded port 9998, so `reader-web/pom.xml` pins `<forkCount>1</forkCount>`. Raising it produces a wall of `BindException: Address already in use`.
- **Some tests hit live third-party sites.** `TestFaviconDownloader` fetches real domains and several cases are `@Ignore`d because those sites died or stopped exposing a favicon. Treat new failures there as environmental until proven otherwise.
- **A failed feed fetch must never be recorded as a successful sync.** `FeedService.checkUrl` rethrows connection-level failures, and `getUrlStrategy` rejects URLs that resolve to no feed rather than falling back to `ContentUrlStrategy`, which would answer with unrelated News API results.
- **Secrets were purged from history.** Two Groq API keys were committed to this project in March 2025, in `reader-web/summarizer.py` and `reader-web/tokens.env`. Both were removed from every commit before this repository was published, and `tokens.env` is gone entirely. `.gitignore` now blocks `.env`, `*.env` and `tokens.env`. The summariser reads `GROQ_API` from `reader-web/tokens.env`, so copy `tokens.env.example` and fill it in locally; do not commit it. A third key, for NewsAPI, was hardcoded in `ContentUrlStrategy` and is now read from the environment. All three should be treated as compromised and rotated.
- **`.travis.yml` is dead.** It predates the fork and points at Sismics' own Docker Hub account. It is kept for reference, not because it runs.
- **The data directory is outside the repository.** Wiping `target/` does not reset the app. Delete `<reader.home>/db` and `<reader.home>/lucene` for that.
- **The Lucene index can drift** from the database if the process is killed mid-write. `POST /api/app/batch/reindex` rebuilds it.
- **`rssman.ai` at the root is an asset source,** the Illustrator file behind the `rssman.png` mascot the web UI and the Android app both use. It is not a stray file.
