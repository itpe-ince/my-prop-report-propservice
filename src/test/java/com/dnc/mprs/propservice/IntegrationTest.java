package com.dnc.mprs.propservice;

import com.dnc.mprs.propservice.config.AsyncSyncConfiguration;
import com.dnc.mprs.propservice.config.EmbeddedElasticsearch;
import com.dnc.mprs.propservice.config.EmbeddedKafka;
import com.dnc.mprs.propservice.config.EmbeddedRedis;
import com.dnc.mprs.propservice.config.EmbeddedSQL;
import com.dnc.mprs.propservice.config.JacksonConfiguration;
import com.dnc.mprs.propservice.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = { PropserviceApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class }
)
@EmbeddedRedis
@EmbeddedElasticsearch
@EmbeddedSQL
@EmbeddedKafka
public @interface IntegrationTest {
}
