package openwt.interview.coding.challenge.config;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseBootConfig {
		
	@Autowired
	private DataSource ds;
	
	@Bean("liquibase")
	public SpringLiquibase getSpringLiquibase() {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(this.ds);
		liquibase.setChangeLog("classpath:db/changelogs/changelog-master.xml");
		return liquibase;
	}
	
}
