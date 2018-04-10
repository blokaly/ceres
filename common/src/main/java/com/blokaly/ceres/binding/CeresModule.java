package com.blokaly.ceres.binding;

import com.google.inject.PrivateModule;
import com.google.inject.binder.AnnotatedBindingBuilder;

public abstract class CeresModule extends PrivateModule {

  protected final <T> AnnotatedBindingBuilder<T> bindExpose(Class<T> clazz) {
    AnnotatedBindingBuilder<T> binding = bind(clazz);
    expose(clazz);
    return binding;
  }
}
