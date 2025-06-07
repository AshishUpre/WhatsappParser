package com.ashupre.whatsappparser.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EnvConfig {

    private final Environment environment;

    private Dotenv dotenv;

    /**
     * loading environment variables & also manually setting oauth credentials
     * # get from console.cloud.google.com -> create new project / existing project -> left panel -> APIs & Services
     * # -> Credentials -> + create credentials (at top)
     * # first you need to configure consent screen for that
     * # whether using for internal or external -> if chosen internal only Google Workspace accounts can log in (companies buying)
     * # external -> users can use their gmail account
     */
    @PostConstruct
    public void loadEnvVariables() {
        String[] profiles = environment.getActiveProfiles();
        String profile = profiles.length > 0 ? profiles[0] : "default";
        String fileName = Objects.equals(profile, "default") ? ".env" : ".env." + profile;
        System.out.println("Active Profile: " + profile);

        dotenv = Dotenv.configure()
                .filename(fileName)
                .ignoreIfMissing()
                .load();

        // setting the values for spring to pick them up via ${VAR_NAME} [in application.properties]
        setEnvVar("GOOGLE_CLIENT_ID", dotenv);
        setEnvVar("GOOGLE_CLIENT_SECRET", dotenv);
        setEnvVar("MONGO_URI", dotenv);
        setEnvVar("GOOGLE_REDIRECT_URL", dotenv);
        setEnvVar("AES_SECRET_KEY", dotenv);
        setEnvVar("AES_IV", dotenv);
        setEnvVar("JWT_SECRET", dotenv);
    }

    private void setEnvVar(String key, Dotenv dotenv) {
        String value = dotenv.get(key);
        log.debug("Env variable: " + key + " = " + value);
        if (value != null) {
            System.setProperty(key, value);
        }
    }

    @Bean
    public String ec2Url() {
        return dotenv.get("EC2_URL");
    }

    @Bean
    public String folderId() {
        return dotenv.get("GOOGLE_DRIVE_FOLDER_ID");
    }

    @Bean
    public String secretKey() {
        return dotenv.get("AES_SECRET_KEY");
    }

    @Bean
    public String iv() {
        return dotenv.get("AES_IV");
    }

    @Bean
    public String jwtSecret() {
        return dotenv.get("JWT_SECRET");
    }
}
