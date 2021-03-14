package com.replaymod.lib.de.johni0702.minecraft.gui.utils.lwjgl.mapped;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface MappedField {
  long byteOffset() default -1L;
  
  long byteLength() default -1L;
}
