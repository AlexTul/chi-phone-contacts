package com.tuleninov.chiphonecontacts;

import com.tuleninov.chiphonecontacts.config.security.properties.CustomSecurityProperties;
import com.tuleninov.chiphonecontacts.model.auth.request.SignInRequest;
import com.tuleninov.chiphonecontacts.model.auth.response.AccessTokenResponse;
import com.tuleninov.chiphonecontacts.model.contact.request.SaveContactRequest;
import com.tuleninov.chiphonecontacts.model.contact.response.ContactResponse;
import com.tuleninov.chiphonecontacts.model.user.request.SaveUserRequest;
import com.tuleninov.chiphonecontacts.repository.AuthorityRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CustomSecurityProperties.class)
class ChiPhoneContactsApplicationTests {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    private static final Logger log = LoggerFactory.getLogger(ChiPhoneContactsApplicationTests.class);

    @Autowired
    private TestRestTemplate rest;
    @Autowired
    private CustomSecurityProperties securityProperties;

    @Test
    void testContextLoads() {
        assertNotNull(rest);
        log.info("TestRestTemplate is not null");
    }

    // region user

    @Test
    void testLogin() {
        ResponseEntity<AccessTokenResponse> response = getLoginResponseEntity();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        AccessTokenResponse accessTokenResponse = response.getBody();
        assertNotNull(accessTokenResponse);
        assertNotNull(accessTokenResponse.accessToken());
        assertNotNull(accessTokenResponse.refreshToken());
        assertEquals(getAccessExpireIn(), accessTokenResponse.expireIn());
        assertEquals(AuthorityRepository.ADMIN_AUTHORITIES, accessTokenResponse.authorities());
    }

    private long getAccessExpireIn() {
        return securityProperties.getJwt().getAccessExpireIn().getSeconds();
    }

    private ResponseEntity<AccessTokenResponse> getLoginResponseEntity() {
        SignInRequest body = getBody();
        HttpHeaders headers = getHeaders();
        HttpEntity<SignInRequest> requestEntity = new HttpEntity<>(body, headers);

        return rest.exchange(
                baseURLLogin(),
                HttpMethod.POST,
                requestEntity,
                AccessTokenResponse.class
        );
    }

    private SignInRequest getBody() {
        List<SaveUserRequest> userRequest = securityProperties.getAdmins().entrySet().stream()
                .map(entry -> new SaveUserRequest(
                        entry.getValue().getEmail(),
                        new String(entry.getValue().getPassword()),
                        entry.getKey()))
                .peek(admin -> log.info("Default admin found: {} <{}>", admin.nickname(), admin.email())).toList();

        return new SignInRequest(
                userRequest.get(0).email(),
                userRequest.get(0).password()
        );
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private URI baseURLLogin() {
        return URI.create(Routes.TOKEN);
    }

    // endregion user

    // region contact

    @Test
    void testCreate() {
        var name = "first";
        var emails = List.of("aaa@gmail.com", "bbb@gmail.com");
        var phones = List.of("+380939333341", "+380939333342", "+380939333343");

        ResponseEntity<ContactResponse> contactResponseEntity = createContact(name, emails, phones);

        assertEquals(HttpStatus.CREATED, contactResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, contactResponseEntity.getHeaders().getContentType());

        ContactResponse responseBody = contactResponseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(name, responseBody.name());
        assertEquals(emails, responseBody.emails());
        assertEquals(phones, responseBody.phones());
    }

    private ResponseEntity<ContactResponse> createContact(String name, List<String> emails, List<String> phones) {
        URI url = baseUrlContact();
        SaveContactRequest requestBody = new SaveContactRequest(name, emails, phones);
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<SaveContactRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        return rest.postForEntity(url, requestEntity, ContactResponse.class);
    }

    private URI baseUrlContact() {
        return URI.create(Routes.CONTACTS);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getToken());
        return headers;
    }

    private String getToken() {
        ResponseEntity<AccessTokenResponse> response = getLoginResponseEntity();
        return Objects.requireNonNull(response.getBody()).accessToken();
    }

    // endregion contact
}
