package org.cdc.generator.elements;

import com.google.j2objc.annotations.UsedByReflection;
import net.mcreator.element.GeneratableElement;
import net.mcreator.workspace.elements.ModElement;
import org.cdc.generator.elements.interfaces.IBlocklyCategory;
import org.cdc.generator.utils.Utils;

import java.awt.*;

public class ProcedureCategoryModElement extends GeneratableElement implements IBlocklyCategory {
    public Color color;
    // mapped parent_category
    public String parentCategory;

    public ProcedureCategoryModElement(ModElement element) {
        super(element);
    }

    @UsedByReflection public String getColor() {
        return Utils.convertColor(color);
    }

    @UsedByReflection public String getParentCategory() {
        if (parentCategory != null && parentCategory.isBlank()) {
            return null;
        }
        return parentCategory;
    }

    @Override public String getBlocklyFolder() {
        return "procedures";
    }
}
