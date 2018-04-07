package com.blokaly.ceres.binding;

import com.google.common.util.concurrent.Service;
import org.reflections.Reflections;

import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

  public static Set<Class<? extends Service>> getAllAnnotatedServices() {
    Reflections reflections = new Reflections("com.blokaly.ceres");
    Set<Class<? extends Service>> services = reflections.getSubTypesOf(Service.class);
    return services.stream().filter(aClass -> aClass.isAnnotationPresent(CeresService.class)).collect(Collectors.toSet());
  }
}
