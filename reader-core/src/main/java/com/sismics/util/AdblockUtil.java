```java
package com.sismics.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.sismics.util.adblock.Helper;
import com.sismics.util.adblock.JSEngine;
import com.sismics.util.adblock.Subscription;
import com.sismics.util.adblock.SubscriptionParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdblockUtil {

    private static final Logger log = LoggerFactory.getLogger(AdblockUtil.class);

    private final List<Subscription> subscriptions;
    private final JSEngine js;
    private boolean interactive;

    public static void main(String[] args) throws Exception {
        AdblockUtil util = new AdblockUtil();
        util.start();
        util.startInteractive();

        // Mock matches method
        String url = "http://example.com";
        String query = "foo=bar";
        String reqHost = "example.com";
        String refHost = "referrer.com";
        String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        boolean matched = util.matches(url, query, reqHost, refHost, accept);
        System.out.println("Matches: " + matched);

        util.stopInteractive();
    }

    public AdblockUtil() {
        subscriptions = new ArrayList<>();
        js = new JSEngine();
        interactive = false;
    }

    public void start() throws Exception {
        js.put("_locale", Locale.getDefault().toString());
        js.put("_datapath", "");
        js.put("_separator", File.separator);
        js.put("_version", "");
        js.put("Android", new Helper(js));
        URL url = Resources.getResource("adblock" + File.separator + "js" + File.separator + "start.js");
        js.evaluate(Resources.toString(url, Charsets.UTF_8));
        loadSubscriptions();
    }

    private void loadSubscriptions() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(AdblockUtil.class.getResourceAsStream("/adblock/subscriptions.xml"), new SubscriptionParser(subscriptions));
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public Subscription getSubscription(String url) {
        for (Subscription subscription : subscriptions) {
            if (subscription.url.equals(url)) {
                return subscription;
            }
        }
        return null;
    }

    public void setSubscription(Subscription subscription) throws Exception {
        if (subscription != null) {
            JSONObject jsonSub = new JSONObject();
            jsonSub.put("url", subscription.url);
            jsonSub.put("title", subscription.title);
            jsonSub.put("homepage", subscription.homepage);
            js.evaluate("clearSubscriptions()");
            js.evaluate("addSubscription(\"" + StringEscapeUtils.escapeJavaScript(jsonSub.toString()) + "\")");
        }
    }

    public void refreshSubscription() throws ScriptException {
        js.evaluate("refreshSubscriptions()");
    }

    public Subscription offerSubscription() {
        Subscription selectedItem = null;
        String selectedPrefix = null;
        int matchCount = 0;
        for (Subscription subscription : subscriptions) {
            if (selectedItem == null) {
                selectedItem = subscription;
            }

            String prefix = checkLocalePrefixMatch(subscription.prefixes);
            if (prefix != null) {
                if (selectedPrefix == null || selectedPrefix.length() < prefix.length()) {
                    selectedItem = subscription;
                    selectedPrefix = prefix;
                    matchCount = 1;
                } else if (selectedPrefix != null && selectedPrefix.length() == prefix.length()) {
                    matchCount++;

                    // If multiple items have a matching prefix of the
                    // same length select one of the items randomly,
                    // probability should be the same for all items.
                    // So we replace the previous match here with
                    // probability 1/N (N being the number of matches).
                    if (Math.random() * matchCount < 1) {
                        selectedItem = subscription;
                        selectedPrefix = prefix;
                    }
                }
            }
        }
        return selectedItem;
    }

    public boolean verifySubscriptions() throws ScriptException {
        return (Boolean) js.evaluate("verifySubscriptions()");
    }

    public Boolean matches(String url, String query, String reqHost, String refHost, String accept) throws Exception {
        return (Boolean) js.evaluate("matchesAny('"
            + StringEscapeUtils.escapeJavaScript(url) + "', '"
            + StringEscapeUtils.escapeJavaScript(query) + "', '"
            + (reqHost != null ? StringEscapeUtils.escapeJavaScript(reqHost) : "") + "', '"
            + (refHost != null ? StringEscapeUtils.escapeJavaScript(refHost) : "") + "', '"
            + (accept != null ? StringEscapeUtils.escapeJavaScript(accept) : "") + "');");
    }

    public void startInteractive() throws ScriptException {
        js.evaluate("startInteractive()");
        interactive = true;
    }

    public void stopInteractive() throws ScriptException {
        js.evaluate("stopInteractive()");
        interactive = false;
    }

    public String checkLocalePrefixMatch(String[] prefixes) {
        if (prefixes == null || prefixes.length == 0) {
            return null;
        }

        String locale = Locale.getDefault().toString().toLowerCase();

        for (int i = 0; i < prefixes.length; i++) {
            if (locale.startsWith(prefixes[i].toLowerCase())) {
                return prefixes[i];
            }
        }

        return null;
    }

    private static class Subscription implements Comparable<Subscription> {
        private final String url;
        private final String title;
        private final String homepage;
        private final String[] prefixes;

        public Subscription(String url, String title, String homepage, String[] prefixes) {
            this.url = url;
            this.title = title;
            this.homepage = homepage;
            this.prefixes = prefixes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Subscription that = (Subscription) o;
            return Objects.equals(url, that.url) &&
                    Objects.equals(title, that.title) &&
                    Objects.equals(homepage, that.homepage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, title, homepage);
        }

        @Override
        public int compareTo(Subscription other) {
            return this.url.compareTo(other.url);
        }
    }
}