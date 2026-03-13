package org.cdc.generator.utils.interfaces;

import com.google.gson.JsonObject;

import java.awt.*;
import java.util.ServiceLoader;

public interface IArg0Type {

    ServiceLoader<IArg0Type> arg0types = ServiceLoader.load(IArg0Type.class,
            IExamplesProvider.class.getClassLoader());

    String name();

    Component getEditor(JsonObject jsonObject);
}
