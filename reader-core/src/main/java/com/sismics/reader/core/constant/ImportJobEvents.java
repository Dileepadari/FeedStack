package com.sismics.reader.core.constant;

public class ImportJobEvents {
    /**
     * Import Job.
     */
    public static final String JOB_IMPORT = "import";

    /**
     * Import Job : number of feeds event.
     */
    public static final String JOB_EVENT_FEED_COUNT = "import.feed_count";

    /**
     * Import Job : number of starred articles' event.
     */
    public static final String JOB_EVENT_STARRED_ARTICLED_COUNT = "import.starred_article_count";

    /**
     * Import Job : feed import success.
     */
    public static final String JOB_EVENT_FEED_IMPORT_SUCCESS = "import.feed_import_success";

    /**
     * Import Job : feed import failure.
     */
    public static final String JOB_EVENT_FEED_IMPORT_FAILURE = "import.feed_import_failure";

    /**
     * Import Job : starred article import success.
     */
    public static final String JOB_EVENT_STARRED_ARTICLE_IMPORT_SUCCESS = "import.starred_article_import_success";

    /**
     * Import Job : starred article failure.
     */
    public static final String JOB_EVENT_STARRED_ARTICLE_IMPORT_FAILURE = "import.starred_article_import_failure";
}
