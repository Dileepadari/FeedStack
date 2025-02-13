File 2:
```java
package com.sismics.util.jpa;

import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.jpa.filter.FilterCriteria;

import java.util.List;
import java.util.Map;

public class QueryParam {
    private final String queryString;
    private final List<String> criteriaList;
    private final Map<String, Object> parameterMap;
    private final SortCriteria sortCriteria;
    private final FilterCriteria filterCriteria;
    private final List<String> groupByList;
    private final ResultMapper resultMapper;

    public String getQueryString() {
        return queryString;
    }

    public SortCriteria getSortCriteria() {
        return sortCriteria;
    }

    public List<String> getCriteriaList() {
        return criteriaList;
    }

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public FilterCriteria getFilterCriteria() {
        return filterCriteria;
    }

    public List<String> getGroupByList() {
        return groupByList;
    }

    public ResultMapper getResultMapper() {
        return resultMapper;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String queryString;
        private List<String> criteriaList;
        private Map<String, Object> parameterMap;
        private SortCriteria sortCriteria;
        private FilterCriteria filterCriteria;
        private List<String> groupByList;
        private ResultMapper resultMapper;

        public Builder withQueryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder withCriteriaList(List<String> criteriaList) {
            this.criteriaList = criteriaList;
            return this;
        }

        public Builder withParameterMap(Map<String, Object> parameterMap) {
            this.parameterMap = parameterMap;
            return this;
        }

        public Builder withSortCriteria(SortCriteria sortCriteria) {
            this.sortCriteria = sortCriteria;
            return this;
        }

        public Builder withFilterCriteria(FilterCriteria filterCriteria) {
            this.filterCriteria = filterCriteria;
            return this;
        }

        public Builder withGroupByList(List<String> groupByList) {
            this.groupByList = groupByList;
            return this;
        }

        public Builder withResultMapper(ResultMapper resultMapper) {
            this.resultMapper = resultMapper;
            return this;
        }

        public QueryParam build() {
            return new QueryParam(queryString, criteriaList, parameterMap, sortCriteria, filterCriteria, groupByList, resultMapper);
        }
    }

    private QueryParam(String queryString, List<String> criteriaList, Map<String, Object> parameterMap, SortCriteria sortCriteria, FilterCriteria filterCriteria, List<String> groupByList,
                      ResultMapper resultMapper) {
        this.queryString = queryString;
        this.criteriaList = criteriaList;
        this.parameterMap = parameterMap;
        this.sortCriteria = sortCriteria;
        this.filterCriteria = filterCriteria;
        this.groupByList = groupByList;
        this.resultMapper = resultMapper;
    }
}
```