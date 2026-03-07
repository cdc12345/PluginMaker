package org.cdc.generator.services;

import jdk.jfr.Description;
import net.mcreator.util.DesktopUtils;
import org.cdc.generator.elements.APIModElement;
import org.cdc.generator.utils.interfaces.IExamplesProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.io.IOException;

@Description("Gradles")
// It is cursed....
public class CurseMavenExamplesProvider implements IExamplesProvider {

    @Override public void provideExamples(JToolBar toolBar, RSyntaxTextArea rSyntaxTextArea, String[] args) {
        JButton curseMaven = new JButton("Curse");
        curseMaven.setToolTipText("CurseMaven");
        toolBar.add(curseMaven);
        curseMaven.addActionListener(a -> {
            DesktopUtils.browseSafe("https://cursemaven.com");
            try (var stream = APIModElement.class.getResourceAsStream("/quilt-1.7.10/templates/apis/cursemaven.ftl")) {
                if (stream != null) {
                    rSyntaxTextArea.setText(new String(stream.readAllBytes()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
