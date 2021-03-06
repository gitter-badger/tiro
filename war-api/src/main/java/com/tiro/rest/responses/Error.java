package com.tiro.rest.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Stream;

/** Error info serializable to JSON. */
@SuppressWarnings({"unused"})
public class Error {
  @JsonProperty("@type") public final String type = "error";
  public final int code;
  public final String url;
  @JsonProperty("class") public final String className;
  public final String message;
  public final String[] stack;

  public final Error inner;

  @JsonCreator
  private Error(@JsonProperty("code") final int code,
                @JsonProperty("url") final String url,
                @JsonProperty("class") final String clazz,
                @JsonProperty("message") final String message,
                @JsonProperty("stack") final String[] stack,
                @JsonProperty("inner") final Error inner) {
    this.code = code;
    this.url = url;
    this.className = clazz;
    this.message = message;
    this.stack = stack;
    this.inner = inner;
  }

  public static Error from(final int code, final String url, final Throwable throwable) {
    final StringWriter out = new StringWriter();
    final PrintWriter writer = new PrintWriter(out);
    throwable.printStackTrace(writer);

    // do some cleanup from TABs and split by 'line end' char
    final String[] lines = Stream.of(out.toString().split("[\\r\\n]+"))
        .map(l -> l.replace("\t", "    "))
        .toArray(String[]::new);

    // compose error
    return new Error(code, url,
        throwable.getClass().getName(),
        throwable.getMessage(),
        lines,
        null == throwable.getCause() ? null : Error.from(-1, null, throwable.getCause()));
  }

  public static Error from(@Nullable final HttpServletRequest request, final Throwable throwable) {
    // default error code
    final String url = (null != request) ? request.getRequestURL().toString() : "";
    int code = 500;

    // extract correct status code
    if (throwable instanceof WebApplicationException) {
      final WebApplicationException wae = (WebApplicationException) throwable;
      code = wae.getResponse().getStatus();
    }

    return from(code, url, throwable);
  }
}
