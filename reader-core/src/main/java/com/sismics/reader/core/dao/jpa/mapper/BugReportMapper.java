package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.constant.BugStatus;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.util.jpa.ResultMapper;

public class BugReportMapper extends ResultMapper<BugReportDto> {
    @Override
    public BugReportDto map(Object[] o) {
        int i = 0;
        BugReportDto dto = new BugReportDto();
        // Long value for setid
        dto.setId(longValue(o[i++]));
        dto.setEmail(stringValue(o[i++]));
        dto.setDescription(stringValue(o[i++]));
        dto.setTimestamp(longValue(o[i++]));
        dto.setStatus(BugStatus.valueOf(stringValue(o[i])));

        return dto;
    }
}