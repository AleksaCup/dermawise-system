package com.ftn.sbnz.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ftn.sbnz.service.dto.QuestionnaireExample;
import com.ftn.sbnz.service.dto.QuestionnaireRequest;
import com.ftn.sbnz.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionnaireExampleService {

    private record Example(String key, String file, boolean pregnant, String title, String description) {
    }

    private static final List<Example> EXAMPLES = List.of(
            new Example("moderate-acne", "moderate-acne", false, "Umerene akne, fototip II",
                    "GAGS 23, adapalen i benzoil peroksid razdvojeni na jutro i veče"),
            new Example("moderate-acne-pregnant", "moderate-acne", true, "Umerene akne u trudnoći",
                    "Isti upitnik uz trudnoću: adapalen se povlači, umesto njega azelainska kiselina"),
            new Example("acne-dark-skin", "acne-dark-skin", false, "Umerene akne, fototip V",
                    "Azelainska kiselina umesto adapalena, čeka potvrdu dermatologa"),
            new Example("rosacea-ppr", "rosacea-ppr", false, "Papulopustularna rozaceja",
                    "Rozaceja PPR, akne isključene jer nema komedona"),
            new Example("perioral-dermatitis", "perioral-dermatitis", false, "Perioralni dermatitis",
                    "Papule oko usta posle kortikosteroidne kreme"),
            new Example("atopic-dermatitis", "atopic-dermatitis", false, "Atopijski dermatitis",
                    "SCORAD 38.5, umeren oblik"),
            new Example("known-allergen", "known-allergen", false, "Akne i alergija na konzervans",
                    "Alergija na methylisothiazolinone, šampon sa Kathon CG dobija upozorenje"));

    private final ObjectMapper objectMapper;

    public List<QuestionnaireExample> all() {
        return EXAMPLES.stream().map(this::toDto).toList();
    }

    public QuestionnaireRequest load(String key) {
        return EXAMPLES.stream()
                .filter(e -> e.key().equals(key))
                .findFirst()
                .map(this::read)
                .orElseThrow(() -> new NotFoundException("Nepoznat primer upitnika " + key));
    }

    private QuestionnaireExample toDto(Example example) {
        return new QuestionnaireExample(example.key(), example.title(), example.description(), read(example));
    }

    private QuestionnaireRequest read(Example example) {
        try (InputStream in = new ClassPathResource("questionnaires/" + example.file() + ".json").getInputStream()) {
            ObjectNode json = (ObjectNode) objectMapper.readTree(in);
            ((ObjectNode) json.get("lifestyle")).put("pregnant", example.pregnant());
            return objectMapper.treeToValue(json, QuestionnaireRequest.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
