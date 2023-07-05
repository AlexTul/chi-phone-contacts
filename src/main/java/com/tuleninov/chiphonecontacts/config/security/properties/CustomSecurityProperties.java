package com.tuleninov.chiphonecontacts.config.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Class with custom security properties.
 * */
@Validated
@ConfigurationProperties(prefix = "custom.security")
public class CustomSecurityProperties {

    @Valid
    @NestedConfigurationProperty
    private JWTProperties jwt;

    private Map<@NotBlank String, @Valid AdminProperties> admins;

    public JWTProperties getJwt() {
        return jwt;
    }

    public void setJwt(JWTProperties jwt) {
        this.jwt = jwt;
    }

    public Map<String, AdminProperties> getAdmins() {
        return admins;
    }

    public void setAdmins(Map<String, AdminProperties> admins) {
        this.admins = admins;
    }
}
