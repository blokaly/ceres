package com.blokaly.ceres.cex.callback;

import com.blokaly.ceres.cex.event.AuthEvent;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

public class AuthCallbackHandler implements CommandCallbackHandler<AuthEvent> {

  @Override
  public AuthEvent parseJson(JsonElement json, JsonDeserializationContext context) {
    return context.deserialize(json, AuthEvent.class);
  }

}
