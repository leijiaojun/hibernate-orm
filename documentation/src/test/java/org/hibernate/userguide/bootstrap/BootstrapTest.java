/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.userguide.bootstrap;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.hibernate.EmptyInterceptor;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.beanvalidation.BeanValidationIntegrator;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.internal.DefaultAutoFlushEventListener;
import org.hibernate.event.internal.DefaultMergeEventListener;
import org.hibernate.event.internal.DefaultPersistEventListener;
import org.hibernate.event.service.spi.DuplicationStrategy;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import org.junit.Test;

/**
 * @author Vlad Mihalcea
 */
public class BootstrapTest {

	@Test
	public void dummy() {}

	{

		ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
		Integrator customIntegrator = new BeanValidationIntegrator();

		//tag::bootstrap-bootstrap-native-registry-BootstrapServiceRegistry-example[]
		BootstrapServiceRegistryBuilder bootstrapRegistryBuilder =
				new BootstrapServiceRegistryBuilder();
		// add a custom ClassLoader
		bootstrapRegistryBuilder.applyClassLoader( customClassLoader );
		// manually add an Integrator
		bootstrapRegistryBuilder.applyIntegrator( customIntegrator );

		BootstrapServiceRegistry bootstrapRegistry = bootstrapRegistryBuilder.build();
		//end::bootstrap-bootstrap-native-registry-BootstrapServiceRegistry-example[]
	}

	{
		//tag::bootstrap-bootstrap-native-registry-StandardServiceRegistryBuilder-example[]
		// An example using an implicitly built BootstrapServiceRegistry
		StandardServiceRegistryBuilder standardRegistryBuilder =
				new StandardServiceRegistryBuilder();
		//end::bootstrap-bootstrap-native-registry-StandardServiceRegistryBuilder-example[]
	}
	{
		//tag::bootstrap-bootstrap-native-registry-StandardServiceRegistryBuilder-example[]

		// An example using an explicitly built BootstrapServiceRegistry
		BootstrapServiceRegistry bootstrapRegistry =
				new BootstrapServiceRegistryBuilder().build();

		StandardServiceRegistryBuilder standardRegistryBuilder =
				new StandardServiceRegistryBuilder( bootstrapRegistry );
		//end::bootstrap-bootstrap-native-registry-StandardServiceRegistryBuilder-example[]
	}

	{

		try {
			//tag::bootstrap-bootstrap-native-registry-MetadataSources-example[]
			ServiceRegistry standardRegistry =
					new StandardServiceRegistryBuilder().build();

			MetadataSources sources = new MetadataSources( standardRegistry );

			// alternatively, we can build the MetadataSources without passing
			// a service registry, in which case it will build a default
			// BootstrapServiceRegistry to use.  But the approach shown
			// above is preferred
			// MetadataSources sources = new MetadataSources();

			// add a class using JPA/Hibernate annotations for mapping
			sources.addAnnotatedClass( MyEntity.class );

			// add the name of a class using JPA/Hibernate annotations for mapping.
			// differs from above in that accessing the Class is deferred which is
			// important if using runtime bytecode-enhancement
			sources.addAnnotatedClassName( "org.hibernate.example.Customer" );

			// Adds the named hbm.xml resource as a source: which performs the
			// classpath lookup and parses the XML
			sources.addResource( "org/hibernate/example/Order.hbm.xml" );

			// Adds the named JPA orm.xml resource as a source: which performs the
			// classpath lookup and parses the XML
			sources.addResource( "org/hibernate/example/Product.orm.xml" );
			//end::bootstrap-bootstrap-native-registry-MetadataSources-example[]
		}
		catch (Exception ignore) {

		}
	}

	{
		try {
			{
				//tag::bootstrap-native-metadata-source-example[]
				ServiceRegistry standardRegistry =
						new StandardServiceRegistryBuilder().build();

				MetadataSources sources = new MetadataSources( standardRegistry )
						.addAnnotatedClass( MyEntity.class )
						.addAnnotatedClassName( "org.hibernate.example.Customer" )
						.addResource( "org/hibernate/example/Order.hbm.xml" )
						.addResource( "org/hibernate/example/Product.orm.xml" );
				//end::bootstrap-native-metadata-source-example[]
			}

			{
				//tag::bootstrap-native-metadata-builder-example[]
				ServiceRegistry standardRegistry =
						new StandardServiceRegistryBuilder().build();

				MetadataSources sources = new MetadataSources( standardRegistry );

				MetadataBuilder metadataBuilder = sources.getMetadataBuilder();

				// Use the JPA-compliant implicit naming strategy
				metadataBuilder.applyImplicitNamingStrategy(
						ImplicitNamingStrategyJpaCompliantImpl.INSTANCE );

				// specify the schema name to use for tables, etc when none is explicitly specified
				metadataBuilder.applyImplicitSchemaName( "my_default_schema" );

				Metadata metadata = metadataBuilder.build();
				//end::bootstrap-native-metadata-builder-example[]
			}
		}
		catch (Exception ignore) {

		}
	}

	{
		try {
			{
				//tag::bootstrap-native-SessionFactory-example[]
				StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
					.configure( "org/hibernate/example/hibernate.cfg.xml" )
					.build();

				Metadata metadata = new MetadataSources( standardRegistry )
					.addAnnotatedClass( MyEntity.class )
					.addAnnotatedClassName( "org.hibernate.example.Customer" )
					.addResource( "org/hibernate/example/Order.hbm.xml" )
					.addResource( "org/hibernate/example/Product.orm.xml" )
					.getMetadataBuilder()
					.applyImplicitNamingStrategy( ImplicitNamingStrategyJpaCompliantImpl.INSTANCE )
					.build();

				SessionFactory sessionFactory = metadata.getSessionFactoryBuilder()
					.applyBeanManager( getBeanManager() )
					.build();
				//end::bootstrap-native-SessionFactory-example[]
			}
			{
				//tag::bootstrap-native-SessionFactoryBuilder-example[]
				StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
						.configure( "org/hibernate/example/hibernate.cfg.xml" )
						.build();

				Metadata metadata = new MetadataSources( standardRegistry )
						.addAnnotatedClass( MyEntity.class )
						.addAnnotatedClassName( "org.hibernate.example.Customer" )
						.addResource( "org/hibernate/example/Order.hbm.xml" )
						.addResource( "org/hibernate/example/Product.orm.xml" )
						.getMetadataBuilder()
						.applyImplicitNamingStrategy( ImplicitNamingStrategyJpaCompliantImpl.INSTANCE )
						.build();

				SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();

				// Supply an SessionFactory-level Interceptor
				sessionFactoryBuilder.applyInterceptor( new CustomSessionFactoryInterceptor() );

				// Add a custom observer
				sessionFactoryBuilder.addSessionFactoryObservers( new CustomSessionFactoryObserver() );

				// Apply a CDI BeanManager ( for JPA event listeners )
				sessionFactoryBuilder.applyBeanManager( getBeanManager() );

				SessionFactory sessionFactory = sessionFactoryBuilder.build();
				//end::bootstrap-native-SessionFactoryBuilder-example[]
			}
		}
		catch (Exception ignore) {

		}
	}

	public Object getBeanManager() {
		return null;
	}

	@Entity
	public static class MyEntity {
		@Id
		private Long id;
	}

	//tag::bootstrap-event-listener-registration-example[]
	public class MyIntegrator implements org.hibernate.integrator.spi.Integrator {

		@Override
		public void integrate(
				Metadata metadata,
				SessionFactoryImplementor sessionFactory,
				SessionFactoryServiceRegistry serviceRegistry) {

			// As you might expect, an EventListenerRegistry is the thing with which event
			// listeners are registered
			// It is a service so we look it up using the service registry
			final EventListenerRegistry eventListenerRegistry =
					serviceRegistry.getService( EventListenerRegistry.class );

			// If you wish to have custom determination and handling of "duplicate" listeners,
			// you would have to add an implementation of the
			// org.hibernate.event.service.spi.DuplicationStrategy contract like this
			eventListenerRegistry.addDuplicationStrategy( new CustomDuplicationStrategy() );

			// EventListenerRegistry defines 3 ways to register listeners:

			// 1) This form overrides any existing registrations with
			eventListenerRegistry.setListeners( EventType.AUTO_FLUSH,
												DefaultAutoFlushEventListener.class );

			// 2) This form adds the specified listener(s) to the beginning of the listener chain
			eventListenerRegistry.prependListeners( EventType.PERSIST,
													DefaultPersistEventListener.class );

			// 3) This form adds the specified listener(s) to the end of the listener chain
			eventListenerRegistry.appendListeners( EventType.MERGE,
												   DefaultMergeEventListener.class );
		}

		@Override
		public void disintegrate(
				SessionFactoryImplementor sessionFactory,
				SessionFactoryServiceRegistry serviceRegistry) {

		}
	}
	//end::bootstrap-event-listener-registration-example[]

	public class CustomDuplicationStrategy implements DuplicationStrategy {

		@Override
		public boolean areMatch(Object listener, Object original) {
			return false;
		}

		@Override
		public Action getAction() {
			return null;
		}
	}

	public class CustomSessionFactoryInterceptor extends EmptyInterceptor {}

	public class CustomSessionFactoryObserver implements SessionFactoryObserver {

		@Override
		public void sessionFactoryCreated(SessionFactory factory) {

		}

		@Override
		public void sessionFactoryClosed(SessionFactory factory) {

		}
	}

	//tag::bootstrap-jpa-compliant-PersistenceUnit-example[]
	@PersistenceUnit
	private EntityManagerFactory emf;
	//end::bootstrap-jpa-compliant-PersistenceUnit-example[]

	{
		try {
			//tag::bootstrap-jpa-compliant-EntityManagerFactory-example[]
			// Create an EMF for our CRM persistence-unit.
			EntityManagerFactory emf = Persistence.createEntityManagerFactory( "CRM" );
			//end::bootstrap-jpa-compliant-EntityManagerFactory-example[]
		} catch (Exception ignore) {}
	}

	{

		try {
			//tag::bootstrap-native-EntityManagerFactory-example[]
			String persistenceUnitName = "CRM";
			List<String> entityClassNames = new ArrayList<>(  );
			Properties properties = new Properties(  );

			PersistenceUnitInfoImpl persistenceUnitInfo = new PersistenceUnitInfoImpl(
					persistenceUnitName,
					entityClassNames,
					properties
			);

			Map<String, Object> integrationSettings = new HashMap<>();
			integrationSettings.put(
					AvailableSettings.INTERCEPTOR,
					new CustomSessionFactoryInterceptor()
			);

			EntityManagerFactoryBuilderImpl entityManagerFactoryBuilder =
					new EntityManagerFactoryBuilderImpl(
							new PersistenceUnitInfoDescriptor( persistenceUnitInfo ),
							integrationSettings
					);

			EntityManagerFactory emf = entityManagerFactoryBuilder.build();
			//end::bootstrap-native-EntityManagerFactory-example[]
		}
		catch (Exception ignore) {
		}
	}

	//tag::bootstrap-native-EntityManagerFactory-example[]

	public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

		private final String persistenceUnitName;

		private PersistenceUnitTransactionType transactionType =
				PersistenceUnitTransactionType.RESOURCE_LOCAL;

		private final List<String> managedClassNames;

		private final Properties properties;

		private DataSource jtaDataSource;

		private DataSource nonJtaDataSource;

		public PersistenceUnitInfoImpl(
				String persistenceUnitName,
				List<String> managedClassNames,
				Properties properties) {
			this.persistenceUnitName = persistenceUnitName;
			this.managedClassNames = managedClassNames;
			this.properties = properties;
		}

		@Override
		public String getPersistenceUnitName() {
			return persistenceUnitName;
		}

		@Override
		public String getPersistenceProviderClassName() {
			return HibernatePersistenceProvider.class.getName();
		}

		@Override
		public PersistenceUnitTransactionType getTransactionType() {
			return transactionType;
		}

		@Override
		public DataSource getJtaDataSource() {
			return jtaDataSource;
		}

		public PersistenceUnitInfoImpl setJtaDataSource(DataSource jtaDataSource) {
			this.jtaDataSource = jtaDataSource;
			this.nonJtaDataSource = null;
			transactionType = PersistenceUnitTransactionType.JTA;
			return this;
		}

		@Override
		public DataSource getNonJtaDataSource() {
			return nonJtaDataSource;
		}

		public PersistenceUnitInfoImpl setNonJtaDataSource(DataSource nonJtaDataSource) {
			this.nonJtaDataSource = nonJtaDataSource;
			this.jtaDataSource = null;
			transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
			return this;
		}

		@Override
		public List<String> getMappingFileNames() {
			return null;
		}

		@Override
		public List<URL> getJarFileUrls() {
			return Collections.emptyList();
		}

		@Override
		public URL getPersistenceUnitRootUrl() {
			return null;
		}

		@Override
		public List<String> getManagedClassNames() {
			return managedClassNames;
		}

		@Override
		public boolean excludeUnlistedClasses() {
			return false;
		}

		@Override
		public SharedCacheMode getSharedCacheMode() {
			return SharedCacheMode.UNSPECIFIED;
		}

		@Override
		public ValidationMode getValidationMode() {
			return ValidationMode.AUTO;
		}

		public Properties getProperties() {
			return properties;
		}

		@Override
		public String getPersistenceXMLSchemaVersion() {
			return "2.1";
		}

		@Override
		public ClassLoader getClassLoader() {
			return Thread.currentThread().getContextClassLoader();
		}

		@Override
		public void addTransformer(ClassTransformer transformer) {

		}

		@Override
		public ClassLoader getNewTempClassLoader() {
			return null;
		}
	}
	//end::bootstrap-native-EntityManagerFactory-example[]
}
