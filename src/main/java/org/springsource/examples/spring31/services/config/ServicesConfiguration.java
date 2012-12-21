package org.springsource.examples.spring31.services.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.ejb.HibernatePersistence;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springsource.examples.spring31.services.Customer;
import org.springsource.examples.spring31.services.CustomerService;

@Configuration
@PropertySource("/config.properties")
@EnableCaching
@EnableTransactionManagement
@ComponentScan(basePackageClasses = { CustomerService.class })
public class ServicesConfiguration {

	@Bean
	public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean()
			throws Exception {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource());
		em.setPackagesToScan(Customer.class.getPackage().getName());
		em.setPersistenceProvider(new HibernatePersistence());
		em.setJpaPropertyMap(contributeJpaEntityManagerProperties());
		return em;
	}

	@Bean
	public PlatformTransactionManager transactionManager() throws Exception {
		return new JpaTransactionManager(localContainerEntityManagerFactoryBean().getObject());
	}

	@Bean
	public DataSource dataSource() throws Exception {
		return new EmbeddedDatabaseBuilder().setName("crm").setType(EmbeddedDatabaseType.H2)
				.build();
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer() throws Exception {
		localContainerEntityManagerFactoryBean();
		DataSourceInitializer initializer = new DataSourceInitializer();
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setScripts(new Resource[] { new ClassPathResource("import.sql") });
		initializer.setDatabasePopulator(populator);
		initializer.setDataSource(dataSource());
		return initializer;
	}

	@Bean
	public CacheManager cacheManager() throws Exception {
		SimpleCacheManager scm = new SimpleCacheManager();
		Cache cache = new ConcurrentMapCache("customers");
		scm.setCaches(Arrays.asList(cache));
		return scm;
	}

	private Map<String, String> contributeJpaEntityManagerProperties() {
		Map<String, String> p = new HashMap<String, String>();
		p.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, "update");
		p.put(org.hibernate.cfg.Environment.DIALECT, H2Dialect.class.getName());
		p.put(org.hibernate.cfg.Environment.SHOW_SQL, "true");
		return p;
	}
}
