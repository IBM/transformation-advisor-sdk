/*
 * (C) Copyright IBM Corp. 2019,2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.ta.sdk.core.assessment;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.ibm.ta.sdk.spi.plugin.TARuntimeException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JavaClassTypeAdapterFactory implements TypeAdapterFactory {
  private static Logger logger = Logger.getLogger(JavaClassTypeAdapterFactory.class.getName());

  private static final String ISSUE_ATTR_JAVA_CLASS = "javaClass";

  final Map<String, TypeAdapter<?>> nameToDelegate = new LinkedHashMap<String, TypeAdapter<?>>();


  public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
    Class<T> rawType = (Class<T>) type.getRawType();
    if (!rawType.equals(IssueRule.class)) {
      return null;
    }

    return new TypeAdapter<T>() {
      public void write(JsonWriter out, T value) throws IOException {
        String javaClass = value.getClass().getName();

        TypeAdapter<T> delegate = null;
        try {
          delegate = (TypeAdapter<T>) getDelegate(gson, javaClass);
        } catch (ClassNotFoundException e) {
          throw new TARuntimeException("Failed to serialize to java class:" + javaClass, e);
        }
        if (delegate == null) {
          throw new TARuntimeException("Failed to serialize to java class:" + javaClass);
        }
        JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
        Streams.write(jsonObject, out);
      }

      public T read(JsonReader reader) throws IOException {
        JsonElement jsonElement = Streams.parse(reader);
        String javaClass;
        JsonElement javaClassJsonElement = jsonElement.getAsJsonObject().get(ISSUE_ATTR_JAVA_CLASS);
        if (javaClassJsonElement != null) {
          javaClass = javaClassJsonElement.getAsString();
        } else {
          javaClass = rawType.getName();
        }
        logger.debug("javaClass:" + javaClass);

        @SuppressWarnings("unchecked") // registration requires that subtype extends T
        TypeAdapter<T> delegate = null;
        try {
          delegate = (TypeAdapter<T>) getDelegate(gson, javaClass);
        } catch (ClassNotFoundException e) {
          throw new TARuntimeException("Failed to deserialize to java class:" + javaClass, e);
        }
        if (delegate == null) {
          throw new TARuntimeException("Failed to deserialize to java class:" + javaClass);
        }
        return delegate.fromJsonTree(jsonElement);
      }
    };
  }

  private TypeAdapter<?> getDelegate(Gson gson, String javaClassName) throws ClassNotFoundException {
    TypeAdapter<?> delegate = nameToDelegate.get(javaClassName);
    if (delegate == null) {
      Class javaClass = Class.forName(javaClassName);
      delegate = gson.getDelegateAdapter(this, TypeToken.get(javaClass));
      if (delegate != null) {
        nameToDelegate.put(javaClassName, delegate);
      }
    }
    return delegate;
  }
}