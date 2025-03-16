package com.sismics.reader.core.dao.jpa.mapper;

import com.sismics.reader.core.constant.BugStatus;
import com.sismics.reader.core.dao.jpa.dto.BugReportDto;
import com.sismics.reader.core.model.jpa.BugReport; // Ensure this import is present
import com.sismics.util.jpa.ResultMapper;

import java.util.ArrayList;
import java.util.List;

public class BugReportMapper extends ResultMapper<BugReportDto> {
    @Override
    public BugReportDto map(Object[] o) {
        int i = 0;
        BugReportDto dto = new BugReportDto();
        dto.setId(stringValue(o[i++]));
        dto.setEmail(stringValue(o[i++]));
        dto.setDescription(stringValue(o[i++]));
        dto.setTimestamp(dateValue(o[i++]));
        dto.setStatus(BugStatus.valueOf(stringValue(o[i++])));
        return dto;
    }

    public List<BugReportDto> mapBugReports(List<BugReport> bugReports) {
        List<BugReportDto> dtos = new ArrayList<>();
        for (BugReport bugReport : bugReports) {
            dtos.add(map(bugReport));
        }
        return dtos;
    }

    public BugReportDto map(BugReport bugReport) {
        BugReportDto dto = new BugReportDto();
        dto.setId(bugReport.getId());
        dto.setEmail(bugReport.getEmail());
        dto.setDescription(bugReport.getDescription());
        dto.setTimestamp(bugReport.getTimestamp());
        dto.setStatus(bugReport.getStatus());
        return dto;
    }
}
