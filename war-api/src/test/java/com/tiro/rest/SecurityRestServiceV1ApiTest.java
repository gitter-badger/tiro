package com.tiro.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tiro.Services;
import com.tiro.entities.Role;
import com.tiro.rest.responses.Error;
import com.tiro.utilities.JsonBinding;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.junit.Test;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Jersey API testing. */
public class SecurityRestServiceV1ApiTest extends TiroJerseyTest {

  @Override
  protected Application configure() {
    return new ResourceConfig(SecurityRestServiceV1.class)
        .property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true)
        .register(ExceptionsMapperService.class);
  }

  private WebTarget getTargetV1Security() {
    return target(Services.V1_SECURITY);
  }

  @Test
  public void testVersionApiCall() throws Exception {
    final String version = getTargetV1Security().path("/version").request().get(String.class);

    assertThat(version)
        .isNotEmpty()
        .containsPattern("[0-9]+(.[0-9]+){1,3}")
        .isEqualToIgnoringCase("1.0");
  }

  @Test
  public void testApplicationWadl() throws Exception {
    final String xml = target().path("application.wadl").request().get(String.class);

    assertThat(xml)
        .isNotEmpty()
        .contains("<?xml")
        .contains("<resource path=\"/v1/security\">");
  }

  @Test
  public void testGetRoles() throws Exception {
    final String json = getTargetV1Security().path("/roles").request().get(String.class);

    assertThat(json).isNotEmpty();

    final List<Role> roles = JsonBinding.fromJson(json, new TypeReference<List<Role>>() {
    });

    assertThat(roles).isNotEmpty();

    _log.info("[0]: {}", roles.get(0));
  }

  @Test
  public void testThrowNotFoundException() throws Exception {
    assertThatThrownBy(() -> target().path("/wrong-url").request().get(String.class))
        .isInstanceOf(InternalServerErrorException.class)
        .hasMessage("HTTP 500 Internal Server Error");
  }

  @Test
  public void testNotFoundExceptionProcessing() throws Exception {
    final Response response = target().path("/wrong-url")
        .request()
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .get();

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(500);
    assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    assertThat(response.hasEntity()).isTrue();

    final Object objMap = response.readEntity(Object.class);
    final Error error = JsonBinding.fromObject(objMap, Error.class);

    assertThat(error).isNotNull();
  }
}
