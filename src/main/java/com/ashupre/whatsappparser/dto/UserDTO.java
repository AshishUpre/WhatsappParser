package com.ashupre.whatsappparser.dto;

import com.ashupre.whatsappparser.util.OAuth2PrincipalUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.security.Principal;

import static com.ashupre.whatsappparser.service.CustomOAuth2UserService.getProviderIdName;

@Getter
@NoArgsConstructor
public class UserDTO {
    private String providerName;
    private String providerId;
    private String name;
    private String givenName;
    private String familyName;
    private String email;
    private boolean emailVerified;
    private String picture;

    public UserDTO(Principal user) {
        if (user instanceof OAuth2AuthenticationToken) {
            String providerName = OAuth2PrincipalUtil.getAttributes(user, "provider");

            this.providerName = providerName;
            this.providerId   = OAuth2PrincipalUtil.getAttributes(user, getProviderIdName(providerName));
            this.name         = OAuth2PrincipalUtil.getAttributes(user, "name");
            this.givenName    = OAuth2PrincipalUtil.getAttributes(user, "given_name");
            this.familyName   = OAuth2PrincipalUtil.getAttributes(user, "family_name");
            this.email        = OAuth2PrincipalUtil.getAttributes(user, "email");
            this.picture      = OAuth2PrincipalUtil.getAttributes(user, "picture");

            Object emailVerifiedObj = ((OAuth2AuthenticationToken) user).getPrincipal().getAttributes().get("email_verified");
            if (emailVerifiedObj instanceof Boolean) {
                this.emailVerified = (Boolean) emailVerifiedObj;
            }
        }
    }

}
