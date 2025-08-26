package com.example.demo.client.adapter;

import com.example.demo.client.adapter.config.CustomerApiClientConfig;
import com.example.demo.client.generated.api.CustomerControllerApi;
import com.example.demo.client.generated.dto.CustomerCreateRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig(classes = {
        CustomerApiClientConfig.class,
        CustomerClientIT.TestBeans.class
})
class CustomerClientIT {

    static MockWebServer server;
    @Autowired
    private CustomerControllerApi api;

    @BeforeAll
    static void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
        System.setProperty("customer.api.base-url", server.url("/customer").toString());
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.shutdown();
        System.clearProperty("customer.api.base-url");
    }

    @Test
    void create_shouldReturn201_andMappedBody() {
        var body = """
                {
                  "status": 201,
                  "message": "CREATED",
                  "data": {
                    "customer": { "id": 1, "name": "Jane Doe", "email": "jane@example.com" },
                    "createdAt": "2025-01-01T12:34:56Z"
                  },
                  "errors": []
                }
                """;

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        CustomerCreateRequest req = new CustomerCreateRequest()
                .name("Jane Doe")
                .email("jane@example.com");

        var resp = api.create(req);

        assertNotNull(resp);
        assertEquals(201, resp.getStatus());
        assertEquals("CREATED", resp.getMessage());
        assertNotNull(resp.getData());
        assertEquals("Jane Doe", resp.getData().getCustomer().getName());
    }

    @Configuration
    static class TestBeans {
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }
}