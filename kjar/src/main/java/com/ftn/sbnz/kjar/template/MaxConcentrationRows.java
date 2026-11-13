package com.ftn.sbnz.kjar.template;

import com.ftn.sbnz.model.template.MaxConcentrationByPhototype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class MaxConcentrationRows {

    public static final String CSV = "templates/data/max-concentration.csv";

    private MaxConcentrationRows() {
    }

    public static List<MaxConcentrationByPhototype> load() {
        InputStream in = MaxConcentrationRows.class.getClassLoader().getResourceAsStream(CSV);
        if (in == null) {
            throw new IllegalStateException("Missing " + CSV);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(MaxConcentrationRows::parse)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static MaxConcentrationByPhototype parse(String line) {
        List<String> cells = split(line);
        if (cells.size() != 5) {
            throw new IllegalStateException("Bad row in " + CSV + ": " + line);
        }
        return new MaxConcentrationByPhototype(
                cells.get(0),
                Integer.parseInt(cells.get(1)),
                Integer.parseInt(cells.get(2)),
                Double.parseDouble(cells.get(3)),
                cells.get(4));
    }

    private static List<String> split(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
            } else if (c == ',' && !quoted) {
                cells.add(cell.toString().trim());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        cells.add(cell.toString().trim());
        return cells;
    }
}
