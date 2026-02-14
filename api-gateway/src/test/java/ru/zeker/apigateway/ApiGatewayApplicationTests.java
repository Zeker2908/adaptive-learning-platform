package ru.zeker.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import ru.zeker.gateway.component.GatewayJwt;
import ru.zeker.gateway.component.JwtValidationFilter;

import static org.mockito.Mockito.*;

@SpringBootTest
class ApiGatewayApplicationTests {

    private final GatewayJwt jwtUtils = mock(GatewayJwt.class);
    private final Jackson2JsonEncoder jsonEncoder = mock(Jackson2JsonEncoder.class);
    private final JwtValidationFilter jwtValidationFilter = new JwtValidationFilter(jwtUtils, jsonEncoder);

    @Test
	void contextLoads() {
	}

    @Test
    public void extractClaimsTest() {

    }
}
