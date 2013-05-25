// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.locators;

import com.google.common.base.Optional;
import com.viadeo.kasper.IDomain;
import com.viadeo.kasper.cqrs.command.ICommand;
import com.viadeo.kasper.cqrs.command.ICommandHandler;
import com.viadeo.kasper.ddd.IAggregateRoot;
import com.viadeo.kasper.ddd.IEntity;
import com.viadeo.kasper.ddd.IInternalDomain;
import com.viadeo.kasper.ddd.IRepository;

import java.util.Collection;
import java.util.Set;

/**
 *
 * The domain locator interface
 * - record domains, entities, repositories and services
 * 
 * TODO: terminate javadoc
 * 
 */
public interface IDomainLocator {

    /**
     * Register a new commandHandler
     */
    void registerHandler(ICommandHandler<? extends ICommand> commandHandler);

    /**
     * Get all registered command handlers
     */
    Collection<ICommandHandler<? extends ICommand>> getHandlers();

	/**
	 * Register a new domain to the locator
	 * 
	 * @param domain the domain to register
	 * @param name the name of the domain
	 * @param prefix the prefix assigned to this domain
	 */
	void registerDomain(IInternalDomain domain, String name, String prefix);

	/**
	 * Return the prefix of a specified domain
	 * 
	 * @param domain the domain
	 * @return the domain prefix
	 */
	String getDomainPrefix(IDomain domain);

	/**
	 * Return the name of a specified domain
	 * 
	 * @param domain the domain
	 * @return the domain name
	 */
	String getDomainName(IDomain domain);

	/**
	 * Search for a domain by name
	 * 
	 * @param name the name of the domain to be searched
	 * @return the (optional) domain
	 */
	<D extends IDomain> Optional<D> getDomainByName(String name);

	/**
	 * Search for a domain by prefix
	 * 
	 * @param prefix the prefix of the domain to be searched
	 * @return the (optional) domain
	 */
	<D extends IDomain> Optional<D> getDomainByPrefix(String prefix);

	/**
	 * @return all registered domains
	 */
	Set<IDomain> getDomains();



	/**
	 * Return the domain related to the specified entity
	 * 
	 * @param entity the entitiy to search for domain
	 * @return the domain of the entity
	 */
	<D extends IDomain> D getEntityDomain(IEntity entity);

	/**
	 * Return a set with all the entities of the specified domain
	 * 
	 * @param domain the domain
	 * @return a set with all entities related to this domain
	 */
	<D extends IDomain> Set<? extends IEntity> getDomainEntities(D domain);

	/**
	 * Return a set with all the entities of the specified domain class
	 * 
	 * @param domain the domain class
	 * @return a set with all entities related to this domain class
	 */
	<D extends IDomain> Set<? extends IEntity> getDomainEntities(Class<D> domain);


	/**
	 * Register a new domain repository
	 * 
	 * @param repository the repository to register
	 */
	void registerRepository(IRepository<?> repository);

	/**
	 * Get the repository for an entity
	 * 
	 * @param entity the entity
	 * @return the repository responsible for storing this entity
	 */
	<E extends IAggregateRoot> IRepository<E> getEntityRepository(E entity);

	/**
	 * Get the repository for an entity class
	 * 
	 * @param entityClass the entity class
	 * @return the repository responsible for storing this class of entities
	 */
	<E extends IAggregateRoot> IRepository<E> getEntityRepository(Class<E> entityClass);

}
