package com.viadeo.kasper.client.platform;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.viadeo.kasper.KasperID;
import com.viadeo.kasper.client.platform.components.eventbus.KasperEventBus;
import com.viadeo.kasper.client.platform.domain.DomainBundle;
import com.viadeo.kasper.client.platform.domain.descriptor.*;
import com.viadeo.kasper.cqrs.command.CommandHandler;
import com.viadeo.kasper.cqrs.command.RepositoryManager;
import com.viadeo.kasper.cqrs.command.impl.DefaultCommandGateway;
import com.viadeo.kasper.cqrs.command.impl.DefaultRepositoryManager;
import com.viadeo.kasper.cqrs.query.QueryHandler;
import com.viadeo.kasper.cqrs.query.impl.DefaultQueryGateway;
import com.viadeo.kasper.ddd.Domain;
import com.viadeo.kasper.ddd.repository.Repository;
import com.viadeo.kasper.er.Concept;
import com.viadeo.kasper.event.EventListener;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.*;

public class PlatformBuilderUTest {

    @Test(expected = NullPointerException.class)
    public void addDomainBundle_withNullAsDomainBundle_shouldThrownException(){
        // Given
        DomainBundle domainBundle = null;
        NewPlatform.Builder builder = new NewPlatform.Builder();

        // When
        builder.addDomainBundle(domainBundle);

        // Then throws an exception
    }

    @Test
    public void addDomainBundle_withDomainBundle_shouldBeOk(){
        // Given
        DomainBundle domainBundle = mock(DomainBundle.class);
        NewPlatform.Builder builder = new NewPlatform.Builder();

        // When
        builder.addDomainBundle(domainBundle);

        // Then no exception
    }

    @Test(expected = NullPointerException.class)
    public void addPlugin_withNullAsPlugin_shouldThrownException(){
        // Given
        Plugin plugin = null;
        NewPlatform.Builder builder = new NewPlatform.Builder();

        // When
        builder.addPlugin(plugin);

        // Then throws an exception
    }

    @Test
    public void addPlugin_withPlugin_shouldBeOk(){
        // Given
        Plugin plugin = mock(Plugin.class);
        NewPlatform.Builder builder = new NewPlatform.Builder();

        // When
        builder.addPlugin(plugin);

        // Then no exception
    }

    @Test(expected = IllegalStateException.class)
    public void build_withoutCommandGateway_shouldThrownException(){
        // Given
        NewPlatform.Builder builder = new NewPlatform.Builder()
                .withQueryGateway(mock(DefaultQueryGateway.class))
                .withEventBus(mock(KasperEventBus.class))
                .withConfiguration(mock(Config.class));

        // When
        builder.build();

        // Then throws an exception
    }

    @Test(expected = IllegalStateException.class)
    public void build_withoutQueryGateway_shouldThrownException(){
        // Given
        NewPlatform.Builder builder = new NewPlatform.Builder()
                .withCommandGateway(mock(DefaultCommandGateway.class))
                .withEventBus(mock(KasperEventBus.class))
                .withConfiguration(mock(Config.class));

        // When
        builder.build();

        // Then throws an exception
    }

    @Test(expected = IllegalStateException.class)
    public void build_withoutEventBus_shouldThrownException(){
        // Given
        NewPlatform.Builder builder = new NewPlatform.Builder()
                .withQueryGateway(mock(DefaultQueryGateway.class))
                .withCommandGateway(mock(DefaultCommandGateway.class))
                .withConfiguration(mock(Config.class));

        // When
        builder.build();

        // Then throws an exception

    }

    @Test(expected = IllegalStateException.class)
    public void build_withoutConfiguration_shouldThrownException(){
        // Given
        NewPlatform.Builder builder = new NewPlatform.Builder()
                .withQueryGateway(mock(DefaultQueryGateway.class))
                .withCommandGateway(mock(DefaultCommandGateway.class))
                .withEventBus(mock(KasperEventBus.class));

        // When
        builder.build();

        // Then throws an exception
    }

    @Test
    public void build_withQueryGateway_withCommandGateway_withEventBus_withConfiguration_shouldBeOk(){
        // Given
        KasperEventBus eventBus = mock(KasperEventBus.class);
        DefaultCommandGateway commandGateway = mock(DefaultCommandGateway.class);
        DefaultQueryGateway queryGateway = mock(DefaultQueryGateway.class);

        NewPlatform.Builder builder = new NewPlatform.Builder()
                .withQueryGateway(queryGateway)
                .withCommandGateway(commandGateway)
                .withEventBus(eventBus)
                .withConfiguration(mock(Config.class));

        // When
        NewPlatform platform = builder.build();

        // Then
        assertNotNull(platform);
        assertEquals(eventBus, platform.getEventBus());
        assertEquals(commandGateway, platform.getCommandGateway());
        assertEquals(queryGateway, platform.getQueryGateway());
    }

    @Test
    public void build_withDomainBundle_shouldConfiguredtheBundle(){
        // Given
        DomainBundle domainBundle = createMockedDomainBundle(
                Lists.<CommandHandler>newArrayList(),
                Lists.<QueryHandler>newArrayList(),
                Lists.<Repository>newArrayList(),
                Lists.<EventListener>newArrayList()
        );

        KasperEventBus eventBus = mock(KasperEventBus.class);
        DefaultCommandGateway commandGateway = mock(DefaultCommandGateway.class);
        DefaultQueryGateway queryGateway = mock(DefaultQueryGateway.class);
        Config configuration = mock(Config.class);

        NewPlatform.Builder builder = new NewPlatform.Builder()
                .withQueryGateway(queryGateway)
                .withCommandGateway(commandGateway)
                .withEventBus(eventBus)
                .withConfiguration(configuration)
                .addDomainBundle(domainBundle);

        // When
        NewPlatform platform = builder.build();

        // Then
        assertNotNull(platform);
        verify(domainBundle).configure(refEq(new NewPlatform.BuilderContext(configuration, eventBus, commandGateway, queryGateway)));
    }

    @Test
    public void build_withPlugin_shouldInitializedThePlugin(){
        // Given
        Plugin plugin = mock(Plugin.class);
        KasperEventBus eventBus = mock(KasperEventBus.class);
        DefaultCommandGateway commandGateway = mock(DefaultCommandGateway.class);
        DefaultQueryGateway queryGateway = mock(DefaultQueryGateway.class);
        Config configuration = mock(Config.class);

        NewPlatform.Builder builder = new NewPlatform.Builder()
                .withQueryGateway(queryGateway)
                .withCommandGateway(commandGateway)
                .withEventBus(eventBus)
                .withConfiguration(configuration)
                .addPlugin(plugin);

        // When
        NewPlatform platform = builder.build();

        // Then
        assertNotNull(platform);
        verify(plugin).initialize(refEq(platform), (DomainDescriptor[]) anyVararg());
    }

    @Test
    public void build_withDomainBundle_containingCommandHandler_shouldWiredTheComponent(){
        // Given
        CommandHandler commandHandler = mock(CommandHandler.class);

        DomainBundle domainBundle = createMockedDomainBundle(
                Lists.<CommandHandler>newArrayList(commandHandler),
                Lists.<QueryHandler>newArrayList(),
                Lists.<Repository>newArrayList(),
                Lists.<EventListener>newArrayList()
        );

        KasperEventBus eventBus = mock(KasperEventBus.class);
        DefaultCommandGateway commandGateway = mock(DefaultCommandGateway.class);
        DomainDescriptorFactory domainDescriptorFactory = createMockedDomainDescriptorFactory();

        NewPlatform.Builder builder = new NewPlatform.Builder(domainDescriptorFactory)
                .withQueryGateway(mock(DefaultQueryGateway.class))
                .withCommandGateway(commandGateway)
                .withEventBus(eventBus)
                .withConfiguration(mock(Config.class))
                .addDomainBundle(domainBundle);

        // When
        NewPlatform platform = builder.build();

        // Then
        assertNotNull(platform);
        verify(commandGateway).register(refEq(commandHandler));
        verify(commandHandler).setEventBus(refEq(eventBus));
    }

    @Test
    public void build_withDomainBundle_containingQueryHandler_shouldWiredTheComponent(){
        // Given
        QueryHandler queryHandler = mock(QueryHandler.class);

        DomainBundle domainBundle = createMockedDomainBundle(
                Lists.<CommandHandler>newArrayList(),
                Lists.<QueryHandler>newArrayList(queryHandler),
                Lists.<Repository>newArrayList(),
                Lists.<EventListener>newArrayList()
        );

        KasperEventBus eventBus = mock(KasperEventBus.class);
        DefaultQueryGateway queryGateway = mock(DefaultQueryGateway.class);
        DomainDescriptorFactory domainDescriptorFactory = createMockedDomainDescriptorFactory();

        NewPlatform.Builder builder = new NewPlatform.Builder(domainDescriptorFactory)
                .withQueryGateway(queryGateway)
                .withCommandGateway(mock(DefaultCommandGateway.class))
                .withEventBus(eventBus)
                .withConfiguration(mock(Config.class))
                .addDomainBundle(domainBundle);

        // When
        NewPlatform platform = builder.build();

        // Then
        assertNotNull(platform);
        verify(queryGateway).register(refEq(queryHandler));
    }

    @Test
    public void build_withDomainBundle_containingEventListener_shouldWiredTheComponent(){
        // Given
        EventListener eventListener = mock(EventListener.class);

        DomainBundle domainBundle = createMockedDomainBundle(
                Lists.<CommandHandler>newArrayList(),
                Lists.<QueryHandler>newArrayList(),
                Lists.<Repository>newArrayList(),
                Lists.<EventListener>newArrayList(eventListener)
        );

        KasperEventBus eventBus = mock(KasperEventBus.class);
        DefaultCommandGateway commandGateway = mock(DefaultCommandGateway.class);
        DomainDescriptorFactory domainDescriptorFactory = createMockedDomainDescriptorFactory();

        NewPlatform.Builder builder = new NewPlatform.Builder(domainDescriptorFactory)
                .withQueryGateway(mock(DefaultQueryGateway.class))
                .withCommandGateway(commandGateway)
                .withEventBus(eventBus)
                .withConfiguration(mock(Config.class))
                .addDomainBundle(domainBundle);

        // When
        NewPlatform platform = builder.build();

        // Then
        assertNotNull(platform);
        verify(eventBus).subscribe(refEq(eventListener));
        verify(eventListener).setCommandGateway(refEq(commandGateway));
    }

    @Test
    public void build_withDomainBundle_containingRepository_shouldWiredTheComponent() throws Exception {
        // Given
        Repository repository = spy(new TestRepository());

        DomainBundle domainBundle = createMockedDomainBundle(
                Lists.<CommandHandler>newArrayList(),
                Lists.<QueryHandler>newArrayList(),
                Lists.<Repository>newArrayList(repository),
                Lists.<EventListener>newArrayList()
        );

        KasperEventBus eventBus = mock(KasperEventBus.class);
        DefaultCommandGateway commandGateway = mock(DefaultCommandGateway.class);
        DomainDescriptorFactory domainDescriptorFactory = createMockedDomainDescriptorFactory();
        RepositoryManager repositoryManager = mock(DefaultRepositoryManager.class);

        NewPlatform.Builder builder = new NewPlatform.Builder(domainDescriptorFactory)
                .withQueryGateway(mock(DefaultQueryGateway.class))
                .withCommandGateway(commandGateway)
                .withEventBus(eventBus)
                .withConfiguration(mock(Config.class))
                .withRepositoryManager(repositoryManager)
                .addDomainBundle(domainBundle);

        // When
        NewPlatform platform = builder.build();

        // Then
        assertNotNull(platform);
        verify(repository).setEventBus(refEq(eventBus));
        verify(repositoryManager).register(refEq(repository));
    }

    private DomainDescriptorFactory createMockedDomainDescriptorFactory(){
        DomainDescriptorFactory domainDescriptorFactory = mock(DomainDescriptorFactory.class);
        when(domainDescriptorFactory.createFrom(any(DomainBundle.class))).thenReturn(
                new DomainDescriptor(
                          "FakeDomain"
                        , Domain.class
                        , Lists.<QueryHandlerDescriptor>newArrayList()
                        , Lists.<CommandHandlerDescriptor>newArrayList()
                        , Lists.<RepositoryDescriptor>newArrayList()
                        , Lists.<EventListenerDescriptor>newArrayList()
                )
        );
        return domainDescriptorFactory;
    }

    private DomainBundle createMockedDomainBundle(
            List<CommandHandler> commandHandlers
            , List<QueryHandler> queryHandlers
            , List<Repository> repositories
            , List<EventListener> eventListeners
    ) {
        DomainBundle domainBundle = mock(DomainBundle.class);
        when(domainBundle.getName()).thenReturn("MockedDomain");
        when(domainBundle.getDomain()).thenReturn(new TestDomain());
        when(domainBundle.getCommandHandlers()).thenReturn(commandHandlers);
        when(domainBundle.getQueryHandlers()).thenReturn(queryHandlers);
        when(domainBundle.getRepositories()).thenReturn(repositories);
        when(domainBundle.getEventListeners()).thenReturn(eventListeners);
        return domainBundle;
    }

    private static class TestDomain implements Domain { }

    private static class TestConcept extends Concept {}

    private static class TestRepository extends Repository<TestConcept> {

        public TestRepository() throws Exception {
            Field declaredField = Repository.class.getDeclaredField("initialized");
            declaredField.setAccessible(true);
            declaredField.set(this, true);
        }
        @Override
        protected Optional<TestConcept> doLoad(KasperID aggregateIdentifier, Long expectedVersion) {
            return Optional.absent();
        }

        @Override
        protected void doSave(TestConcept aggregate) { }

        @Override
        protected void doDelete(TestConcept aggregate) {  }

    }
}
