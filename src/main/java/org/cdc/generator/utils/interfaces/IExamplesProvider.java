package org.cdc.generator.utils.interfaces;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.util.ServiceLoader;

/**
 * this interface provide the examples to be convenient to provide examples
 */
public interface IExamplesProvider {
    ServiceLoader<IExamplesProvider> examplesProviders = ServiceLoader.load(IExamplesProvider.class,
            IExamplesProvider.class.getClassLoader());

    void provideExamples(JToolBar toolBar, RSyntaxTextArea rSyntaxTextArea,String[] args);
}
