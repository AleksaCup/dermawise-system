package com.ftn.sbnz.kjar;

import org.kie.api.KieBase;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

import java.util.List;
import java.util.stream.Collectors;

public class KnowledgeBaseFactory {

    public static final List<String> ANALYSIS_RULES = List.of(
            "rules/phototype.drl",
            "rules/diagnosis.drl",
            "rules/therapy.drl",
            "rules/safety.drl",
            "rules/backward.drl");

    public static final List<String> BACKWARD_RULES = List.of(
            "rules/backward.drl",
            "rules/diagnosis.drl");

    public static final List<String> CEP_RULES = List.of(
            "rules/cep_diary.drl",
            "rules/safety.drl",
            "rules/backward.drl");

    public KnowledgeBases buildAll(List<String> generatedDrl) {
        return new KnowledgeBases(
                build(ANALYSIS_RULES, generatedDrl, EventProcessingOption.CLOUD),
                build(BACKWARD_RULES, List.of(), EventProcessingOption.CLOUD),
                build(CEP_RULES, generatedDrl, EventProcessingOption.STREAM));
    }

    public KieBase build(List<String> ruleFiles, List<String> generatedDrl, EventProcessingOption mode) {
        KieHelper helper = new KieHelper();
        ClassLoader classLoader = getClass().getClassLoader();
        for (String file : ruleFiles) {
            helper.addResource(ResourceFactory.newClassPathResource(file, "UTF-8", classLoader), ResourceType.DRL);
        }
        for (int i = 0; i < generatedDrl.size(); i++) {
            helper.addContent(generatedDrl.get(i), "rules/generated" + i + ".drl");
        }

        Results results = helper.verify();
        if (results.hasMessages(Message.Level.ERROR)) {
            String errors = results.getMessages(Message.Level.ERROR).stream()
                    .map(Message::toString)
                    .collect(Collectors.joining("\n"));
            throw new IllegalStateException("Knowledge base has errors:\n" + errors);
        }
        return helper.build(mode);
    }
}
