package com.tuleninov.chiphonecontacts;

import com.tuleninov.chiphonecontacts.config.security.properties.CustomSecurityProperties;
import com.tuleninov.chiphonecontacts.model.auth.request.SignInRequest;
import com.tuleninov.chiphonecontacts.model.auth.response.AccessTokenResponse;
import com.tuleninov.chiphonecontacts.model.contact.request.SaveContactRequest;
import com.tuleninov.chiphonecontacts.model.contact.response.ContactResponse;
import com.tuleninov.chiphonecontacts.model.user.KnownAuthority;
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
import org.springframework.test.context.ActiveProfiles;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"debug"})
@EnableConfigurationProperties(CustomSecurityProperties.class)
class ChiPhoneContactsApplicationTests {

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
        // Создание заголовков HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Создание объекта запроса
        List<SaveUserRequest> requests = securityProperties.getAdmins().entrySet().stream()
                .map(entry -> new SaveUserRequest(
                        entry.getValue().getEmail(),
                        new String(entry.getValue().getPassword()),
                        entry.getKey()))
                .peek(admin -> log.info("Default admin found: {} <{}>", admin.nickname(), admin.email())).toList();
        SignInRequest signInRequest = new SignInRequest(
                requests.get(0).email(),
                requests.get(0).password()
        );

        HttpEntity<SignInRequest> requestEntity = new HttpEntity<>(signInRequest, headers);

        // Выполнение POST-запроса и получение ответа
        ResponseEntity<AccessTokenResponse> response = rest.exchange(
                createURLForLogin(),
                HttpMethod.POST,
                requestEntity,
                AccessTokenResponse.class
        );

        // Проверка статуса ответа
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверка содержимого ответа
        AccessTokenResponse accessTokenResponse = response.getBody();
        assertNotNull(accessTokenResponse);
        assertNotNull(accessTokenResponse.accessToken());
        assertNotNull(accessTokenResponse.refreshToken());
        assertEquals(getAccessExpireIn(), accessTokenResponse.expireIn());
        assertEquals(getAdminKnownAuthorities(), accessTokenResponse.authorities());
    }

    private URI createURLForLogin() {
        return URI.create(Routes.TOKEN);
    }

    private long getAccessExpireIn() {
        return securityProperties.getJwt().getAccessExpireIn().getSeconds();
    }

    @NotNull
    private String getAdminKnownAuthorities() {
        Set<KnownAuthority> sortedAuthorities = new TreeSet<>(Comparator.comparing(Enum::name));
        Set<KnownAuthority> adminAuthorities = AuthorityRepository.ADMIN_AUTHORITIES;
        sortedAuthorities.addAll(adminAuthorities);
        return sortedAuthorities.toString();
    }

    // endregion user

    // region contact

    @Test
    void testCreate() {
        var name = "third";
        var emails = List.of("strf@gmail.com", "numbf@gmail.com");
        var phones = List.of("+380939333339", "+380939333330", "+380939333331");

        ResponseEntity<ContactResponse> contactResponseEntity = createContact(name, emails, phones);

        assertEquals(HttpStatus.CREATED, contactResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, contactResponseEntity.getHeaders().getContentType());

        ContactResponse responseBody = contactResponseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(name, responseBody.name());
        assertNull(responseBody.emails());
        assertNull(responseBody.phones());
    }

    private ResponseEntity<ContactResponse> createContact(String name, List<String> emails, List<String> phones) {
        var url = baseUrlContact();
        var requestBody = new SaveContactRequest(name, emails, phones);
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<SaveContactRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        return rest.postForEntity(url, requestEntity, ContactResponse.class);
    }

    private URI baseUrlContact() {
        return URI.create(Routes.CONTACTS);
    }

    @org.jetbrains.annotations.NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getToken());
        return headers;
    }

    private String getToken() {
        // Создание заголовков HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Создание объекта запроса
        List<SaveUserRequest> requests = securityProperties.getAdmins().entrySet().stream()
                .map(entry -> new SaveUserRequest(
                        entry.getValue().getEmail(),
                        new String(entry.getValue().getPassword()),
                        entry.getKey()))
                .peek(admin -> log.info("Default admin found: {} <{}>", admin.nickname(), admin.email())).toList();
        SignInRequest signInRequest = new SignInRequest(
                requests.get(0).email(),
                requests.get(0).password()
        );

        HttpEntity<SignInRequest> requestEntity = new HttpEntity<>(signInRequest, headers);

        // Выполнение POST-запроса и получение ответа
        ResponseEntity<AccessTokenResponse> response = rest.exchange(
                createURLForLogin(),
                HttpMethod.POST,
                requestEntity,
                AccessTokenResponse.class
        );

        AccessTokenResponse accessTokenResponse = response.getBody();

        assert accessTokenResponse != null;
        return accessTokenResponse.accessToken();
    }

    // endregion contact

}
