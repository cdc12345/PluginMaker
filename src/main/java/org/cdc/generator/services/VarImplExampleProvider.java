package org.cdc.generator.services;

import jdk.jfr.Description;
import net.mcreator.generator.Generator;
import net.mcreator.generator.GeneratorConfiguration;
import net.mcreator.workspace.elements.VariableTypeLoader;
import org.cdc.generator.utils.Utils;
import org.cdc.generator.utils.interfaces.IExamplesProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.util.List;

@Description("VarImplExamples") public class VarImplExampleProvider implements IExamplesProvider {

    @Override public void provideExamples(JToolBar toolBar, RSyntaxTextArea rSyntaxTextArea, String[] args) {
        String generatorName = args[0];
        String scopeName = args[1];
        String definitionName = args[2];
        JButton number = new JButton("Number");
        number.setOpaque(false);
        number.addActionListener(a -> {
            var generator = Generator.GENERATOR_CACHE.get(generatorName);
            if (generator != null) {
                var num = getVariableScope("number", scopeName, definitionName, generator);
                rSyntaxTextArea.setText(String.join("\n", num));
            }
        });
        toolBar.add(number);
        JButton logic = new JButton("Logic");
        logic.setOpaque(false);
        logic.addActionListener(a -> {
            var generator = Generator.GENERATOR_CACHE.get(generatorName);
            if (generator != null) {
                var num = getVariableScope("logic", toolBar.getName(), rSyntaxTextArea.getName(), generator);
                rSyntaxTextArea.setText(String.join("\n", num));
            }
        });
        toolBar.add(logic);
    }

    private static @NonNull List<String> getVariableScope(String variableName, String scopeName, String definitionName,
            GeneratorConfiguration generator) {
        return Utils.convertYamlToList(generator.getVariableTypes()
                .getScopeDefinition(VariableTypeLoader.INSTANCE.fromName(variableName), scopeName).get(definitionName));
    }
}
