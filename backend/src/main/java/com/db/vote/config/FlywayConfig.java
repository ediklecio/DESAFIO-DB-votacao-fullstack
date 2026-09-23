package com.db.vote.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração explícita do Flyway para garantir que as migrações
 * sejam executadas ANTES de o Hibernate validar o schema.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            System.out.println("✓ Executando migrações Flyway...");
            flyway.migrate();
            System.out.println("✓ Migrações Flyway executadas com sucesso!");
        };
    }
}
