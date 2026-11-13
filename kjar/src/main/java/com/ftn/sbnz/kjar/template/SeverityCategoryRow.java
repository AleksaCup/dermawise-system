package com.ftn.sbnz.kjar.template;

import com.ftn.sbnz.model.enums.Severity;

public class SeverityCategoryRow {

    private final String scale;
    private final String min;
    private final String max;
    private final Severity severity;
    private final String source;

    public SeverityCategoryRow(String scale, String min, String max, Severity severity, String source) {
        this.scale = scale;
        this.min = min;
        this.max = max;
        this.severity = severity;
        this.source = source;
    }

    public String getScale() {
        return scale;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getSource() {
        return source;
    }
}
