package com.blokaly.ceres.binding;

import com.google.inject.Provider;

public abstract class ServiceProvider<T> extends BootstrapService implements Provider<T> {
}
