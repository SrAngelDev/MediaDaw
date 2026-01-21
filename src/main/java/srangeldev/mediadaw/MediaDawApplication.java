package srangeldev.mediadaw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableJpaAuditing
public class MediaDawApplication {
    private static final Logger log = LoggerFactory.getLogger(MediaDawApplication.class);
    private final Environment environment;

    public MediaDawApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(MediaDawApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logApplicationStartup() {
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "");

        log.info("═══════════════════════════════════════════════════════════");
        log.info("🚀 Aplicación MediaDaw iniciada correctamente");
        log.info("═══════════════════════════════════════════════════════════");
        log.info("🌐 URL de acceso: http://localhost:{}{}", port, contextPath);
        log.info("🎨 Consola H2: http://localhost:{}/h2-console", port);
        log.info("📊 Perfil activo: {}", String.join(", ", environment.getActiveProfiles()));
        log.info("───────────────────────────────────────────────────────────");
    }

}
