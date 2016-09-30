package org.openlmis.requisition.web;


import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.requisition.domain.Requisition;
import org.openlmis.requisition.dto.ProgramDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;

public class OrderNumberConfigurationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orderNumberConfigurations";
  private static final String UUID_STRING = "5625602e-6f5d-11e6-8b77-86f30ca893d3";

  @Autowired
  private OrderNumberConfigurationRepository orderNumberConfigurationRepository;

  @Autowired
  private OrderRepository orderRepository;

  private Requisition requisition;
  private ProgramDto programDto;

  @Before
  public void setUp() {

    requisition = new Requisition();
    requisition.setId(UUID.fromString(UUID_STRING));
    requisition.setEmergency(true);

    programDto = new ProgramDto();
    programDto.setCode("code");
  }

  @Test
  public void shouldUpdateOrderNumberConfiguration() {
    OrderNumberConfiguration orderNumberConfiguration =
        new OrderNumberConfiguration("prefix", true, true, true);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderNumberConfiguration)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(OrderNumberConfiguration.class);

    assertEquals(1, orderNumberConfigurationRepository.count());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Ignore
  @Test
  public void shouldUseNewestConfigurationWhenConvertingRequisitionToOrder() {
    orderRepository.deleteAll();
    final String prefix = "prefix";

    RestTemplate restTemplate = new RestTemplate();
    OrderNumberConfiguration orderNumberConfiguration =
        new OrderNumberConfiguration(prefix, false, false, false);
    HttpEntity<OrderNumberConfiguration> request = new HttpEntity<>(orderNumberConfiguration);
    restTemplate.postForObject(BASE_URL + RESOURCE_URL,
        request, OrderNumberConfiguration.class);

    orderNumberConfiguration.setIncludeProgramCode(true);
    orderNumberConfiguration.setIncludeRequisitionTypeSuffix(true);
    request = new HttpEntity<>(orderNumberConfiguration);
    restTemplate.postForObject(BASE_URL + RESOURCE_URL,
        request, OrderNumberConfiguration.class);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(Collections.singletonList(requisition))
        .when()
        .post("/api/orders/requisitions")
        .then()
        .statusCode(201);

    Order order = orderRepository.findAll().iterator().next();
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),
        RamlMatchers.hasNoViolations());

    String expected = prefix + requisition.getId().toString() + "E";
    assertEquals(order.getOrderCode(), expected);

  }

  @Ignore
  @Test
  public void shouldUseOldConfigurationWhenNewOneIsIncorrect() {
    orderRepository.deleteAll();

    RestTemplate restTemplate = new RestTemplate();
    OrderNumberConfiguration orderNumberConfiguration =
        new OrderNumberConfiguration(".prefix,,", false, true, false);
    HttpEntity<OrderNumberConfiguration> request = new HttpEntity<>(orderNumberConfiguration);
    restTemplate.postForObject(BASE_URL + RESOURCE_URL,
        request, OrderNumberConfiguration.class);

    orderNumberConfiguration.setIncludeProgramCode(true);
    orderNumberConfiguration.setIncludeRequisitionTypeSuffix(true);
    request = new HttpEntity<>(orderNumberConfiguration);
    restTemplate.postForObject(BASE_URL + RESOURCE_URL,
        request, OrderNumberConfiguration.class);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(Collections.singletonList(requisition))
        .when()
        .post("/api/orders/requisitions")
        .then()
        .statusCode(201);

    Order order = orderRepository.findAll().iterator().next();
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(),
        RamlMatchers.hasNoViolations());
    assertEquals(order.getOrderCode(), UUID_STRING);
  }

}