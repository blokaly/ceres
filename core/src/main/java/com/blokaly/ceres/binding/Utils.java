package com.blokaly.ceres.binding;

import com.google.common.util.concurrent.Service;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

  public static Set<Class<? extends Service>> getAllCeresServices() {
    Reflections reflections = new Reflections("com.blokaly.ceres");
    Set<Class<? extends Service>> services = reflections.getSubTypesOf(Service.class);
    return services.stream().filter(clz -> clz.isAnnotationPresent(CeresService.class) && isInstantiable(clz)).collect(Collectors.toSet());
  }

  private static boolean isInstantiable(Class<? extends Service> clz) {
    return !Modifier.isAbstract(clz.getModifiers()) && !clz.isInterface();
  }
}
