package org.cdc.generator.elements;

import com.google.j2objc.annotations.UsedByReflection;

public interface IGeneratorSpecific {
    @UsedByReflection String getGeneratorName();
}
