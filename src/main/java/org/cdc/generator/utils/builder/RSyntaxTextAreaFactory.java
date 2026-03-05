package org.cdc.generator.utils.builder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class RSyntaxTextAreaFactory {
    public static RSyntaxTextArea createDefaultRSyntaxTextArea(){
        var jTextArea = new RSyntaxTextArea();
        jTextArea.setOpaque(false);
        jTextArea.setRows(15);
        jTextArea.setColumns(60);
        return jTextArea;
    }
}
