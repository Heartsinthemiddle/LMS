package com.lms.dto.response;

import java.util.List;
import java.util.Map;

public class XapiStatementsResponse {

    private List<Map<String, Object>> statements;
    private String more;

    public List<Map<String, Object>> getStatements() {
        return statements;
    }

    public void setStatements(List<Map<String, Object>> statements) {
        this.statements = statements;
    }

    public String getMore() {
        return more;
    }

    public void setMore(String more) {
        this.more = more;
    }
}
