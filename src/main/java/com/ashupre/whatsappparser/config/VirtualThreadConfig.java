package com.ashupre.whatsappparser.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfig {

    /**
     * We are configuring Tomcat to use virtual thread per task executor. It is not like a thread pool where there
     * are fixed number of os threads. Here for every task, one new virtual thread (jvm managed) is created. So there
     * is not thread reuse. But these virtual threads are very lightweight and as they are managed by JVM instead of
     * OS, context switch is much more efficient (no syscalls, no kernel involvement, no storing and loading registers
     * etc.. ).
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return factory -> factory.addConnectorCustomizers(
                connector -> connector.getProtocolHandler()
                        .setExecutor(Executors.newVirtualThreadPerTaskExecutor())
        );
    }
}
