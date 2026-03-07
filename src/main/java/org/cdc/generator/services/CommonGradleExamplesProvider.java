package org.cdc.generator.services;

import jdk.jfr.Description;
import net.mcreator.ui.init.UIRES;
import org.cdc.generator.elements.APIModElement;
import org.cdc.generator.utils.interfaces.IExamplesProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.io.IOException;

@Description("Gradles") public class CommonGradleExamplesProvider implements IExamplesProvider {

    @Override public void provideExamples(JToolBar toolBar, RSyntaxTextArea jTextArea, String[] args) {
        JButton forge = new JButton(UIRES.get("16px.forge"));
        forge.setToolTipText("ForgeGradle");
        toolBar.add(forge);
        JButton neo = new JButton(UIRES.get("16px.neoforge"));
        neo.setToolTipText("ModDevGradle");
        toolBar.add(neo);
        JButton legacyNeo = new JButton(UIRES.get("16px.neoforge"));
        legacyNeo.setToolTipText("NeoLegacyGradle (Generator-1.20.1)");
        toolBar.add(legacyNeo);

        forge.addActionListener(a -> {
            try (var stream = APIModElement.class.getResourceAsStream("/quilt-1.7.10/templates/apis/forgegradle.ftl")) {
                if (stream != null) {
                    jTextArea.setText(new String(stream.readAllBytes()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        neo.addActionListener(a -> {
            try (var stream = APIModElement.class.getResourceAsStream(
                    "/quilt-1.7.10/templates/apis/moddevgradle.ftl")) {
                if (stream != null) {
                    jTextArea.setText(new String(stream.readAllBytes()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        legacyNeo.addActionListener(a -> {
            try (var stream = APIModElement.class.getResourceAsStream(
                    "/quilt-1.7.10/templates/apis/legacymoddevgradle.ftl")) {
                if (stream != null) {
                    jTextArea.setText(new String(stream.readAllBytes()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
