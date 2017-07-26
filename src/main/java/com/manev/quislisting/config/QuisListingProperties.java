package com.manev.quislisting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Properties specific to Quis Listing.
 * <p>
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "quislisting", ignoreUnknownFields = false)
public class QuisListingProperties {

    private final Security security = new Security();
    private final Ribbon ribbon = new Ribbon();
    private final QuisListingProperties.Mail mail = new QuisListingProperties.Mail();
    private final CorsConfiguration cors = new CorsConfiguration();
    private final JcrRepository jcrRepository = new JcrRepository();

    public Security getSecurity() {
        return security;
    }

    public Ribbon getRibbon() {
        return ribbon;
    }

    public QuisListingProperties.Mail getMail() {
        return this.mail;
    }

    public JcrRepository getJcrRepository() {
        return jcrRepository;
    }

    public CorsConfiguration getCors() {
        return this.cors;
    }

    public static class Security {

        private final Authentication authentication = new Authentication();

        public Authentication getAuthentication() {
            return authentication;
        }

        public static class Authentication {

            private final Jwt jwt = new Jwt();

            public Jwt getJwt() {
                return jwt;
            }

            public static class Jwt {

                private String secret;

                private long tokenValidityInSeconds = 1800;

                private long tokenValidityInSecondsForRememberMe = 2592000;

                public String getSecret() {
                    return secret;
                }

                public void setSecret(String secret) {
                    this.secret = secret;
                }

                public long getTokenValidityInSeconds() {
                    return tokenValidityInSeconds;
                }

                public void setTokenValidityInSeconds(long tokenValidityInSeconds) {
                    this.tokenValidityInSeconds = tokenValidityInSeconds;
                }

                public long getTokenValidityInSecondsForRememberMe() {
                    return tokenValidityInSecondsForRememberMe;
                }

                public void setTokenValidityInSecondsForRememberMe(long tokenValidityInSecondsForRememberMe) {
                    this.tokenValidityInSecondsForRememberMe = tokenValidityInSecondsForRememberMe;
                }
            }
        }
    }

    public static class Ribbon {

        private String[] displayOnActiveProfiles;

        public String[] getDisplayOnActiveProfiles() {
            return displayOnActiveProfiles;
        }

        public void setDisplayOnActiveProfiles(String[] displayOnActiveProfiles) {
            this.displayOnActiveProfiles = displayOnActiveProfiles;
        }
    }

    public static class Mail {
        private String from = "";
        private String baseUrl = "";

        public String getFrom() {
            return this.from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getBaseUrl() {
            return this.baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class JcrRepository {
        private String home;

        public String getHome() {
            return home;
        }

        public void setHome(String home) {
            this.home = home;
        }
    }
}
