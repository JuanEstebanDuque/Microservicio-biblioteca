package co.analisys.biblioteca.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    @SuppressWarnings("unchecked")
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> defaultAuthorities =
                Optional.ofNullable(defaultConverter.convert(jwt))
                        .orElse(Collections.emptyList());

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Collection<GrantedAuthority> keycloakRoles = Collections.emptyList();

        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                keycloakRoles = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());
            }
        }

        List<GrantedAuthority> allAuthorities = new ArrayList<>(defaultAuthorities);
        allAuthorities.addAll(keycloakRoles);

        return new JwtAuthenticationToken(jwt, allAuthorities);
    }
}
