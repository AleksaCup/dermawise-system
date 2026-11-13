package com.ftn.sbnz.kjar.template;

import com.ftn.sbnz.model.enums.Severity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class SeverityCategoryRows {

    public static final String CSV = "templates/data/severity-categories.csv";

    private SeverityCategoryRows() {
    }

    public static List<SeverityCategoryRow> load() {
        InputStream in = SeverityCategoryRows.class.getClassLoader().getResourceAsStream(CSV);
        if (in == null) {
            throw new IllegalStateException("Missing " + CSV);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(SeverityCategoryRows::parse)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static SeverityCategoryRow parse(String line) {
        String[] cells = line.split(",");
        if (cells.length != 5) {
            throw new IllegalStateException("Bad row in " + CSV + ": " + line);
        }
        String min = cells[1].trim();
        String max = cells[2].trim();
        if (Double.parseDouble(min) > Double.parseDouble(max)) {
            throw new IllegalStateException("min is greater than max in " + CSV + ": " + line);
        }
        return new SeverityCategoryRow(cells[0].trim(), min, max, Severity.valueOf(cells[3].trim()), cells[4].trim());
    }
}
