package com.ashupre.whatsappparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.security.Principal;
import java.util.Map;

@Getter
@NoArgsConstructor
public class UserDTO {
    private String providerId;
    private String name;
    private String givenName;
    private String familyName;
    private String email;
    private boolean emailVerified;
    private String picture;

    public UserDTO(Principal user) {
        if (user instanceof OAuth2AuthenticationToken token) {
            Map<String, Object> attributes = token.getPrincipal().getAttributes();
            this.providerId = (String) attributes.get("sub");
            this.name = (String) attributes.get("name");
            this.givenName = (String) attributes.get("given_name");
            this.familyName = (String) attributes.get("family_name");
            this.email = (String) attributes.get("email");
            this.emailVerified = (Boolean) attributes.get("email_verified");
            this.picture = (String) attributes.get("picture");
        }
    }
}
