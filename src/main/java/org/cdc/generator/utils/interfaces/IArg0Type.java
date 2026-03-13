package org.cdc.generator.utils.interfaces;

import com.google.gson.JsonObject;
import org.cdc.generator.utils.Arg0InputType;

import java.awt.*;
import java.util.ServiceLoader;

public interface IArg0Type {

    ServiceLoader<IArg0Type> arg0types = ServiceLoader.load(IArg0Type.class, IExamplesProvider.class.getClassLoader());

    String name();

    Component getEditor(JsonObject jsonObject);

    Arg0InputType type();
}
