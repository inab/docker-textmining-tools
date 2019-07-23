package es.bsc.inb.ades.rest.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({ "classpath:ades_rest_api.properties" })
public class PersistenceJPAConfig{


}
