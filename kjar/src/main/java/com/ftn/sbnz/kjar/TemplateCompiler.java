package com.ftn.sbnz.kjar;

import org.drools.template.ObjectDataCompiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;

public class TemplateCompiler {

    public String compile(String templatePath, Collection<?> rows) {
        try (InputStream template = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (template == null) {
                throw new IllegalArgumentException("Template not found: " + templatePath);
            }
            return new ObjectDataCompiler().compile(rows, template);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read template " + templatePath, e);
        }
    }
}
