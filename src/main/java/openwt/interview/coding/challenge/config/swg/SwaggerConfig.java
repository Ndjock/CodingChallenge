package openwt.interview.coding.challenge.config.swg;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
@PropertySource("classpath:swagger.properties") // just  for showing 
//'swagger.properties' will not be purposely used, only for demo, because it makes no point all: not all Swagger annotations 
//are supported by the property resolution mechanism. We would end up with a part documentation
// on the annotations and the other in the properties, which is confused and tricky to maintain
public class SwaggerConfig {
	
	@Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.basePackage("openwt.interview.coding.challenge"))              
          .paths(PathSelectors.any())                          
          .build().apiInfo(getApiInfo());                                           
    }
	
	private ApiInfo getApiInfo() {
		Contact contact = new Contact("Arthur Ndjock-Abanda", "www.arthur-coding-challenge.ch", "arthur@abanda-domain.com");
        return new ApiInfoBuilder()
                .title("OpenWT coding challenge")
                .description("REST API for Contact manager\"")
                .version("1.0.0")
                .license("Apache License Version 2.0")
                .contact(contact)
                .build();
    }
}
