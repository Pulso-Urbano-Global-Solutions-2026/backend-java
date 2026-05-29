package br.com.pulsourbano;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    // STATIC: container sobe UMA vez para toda a suite de testes que estende essa classe.
    // Sem isso, cada classe de teste levaria ~30s de boot. Com isso: ~30s total.
    protected static final OracleContainer ORACLE = new OracleContainer(
            DockerImageName.parse("gvenzl/oracle-free:23-slim-faststart"))
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // reusa entre runs locais se ~/.testcontainers.properties tiver testcontainers.reuse.enable=true

    static { ORACLE.start(); }

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", ORACLE::getJdbcUrl);
        r.add("spring.datasource.username", ORACLE::getUsername);
        r.add("spring.datasource.password", ORACLE::getPassword);
    }
}
