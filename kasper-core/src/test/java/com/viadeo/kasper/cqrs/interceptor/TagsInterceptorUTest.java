// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.cqrs.interceptor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.viadeo.kasper.context.Context;
import com.viadeo.kasper.context.Contexts;
import com.viadeo.kasper.context.Tags;
import com.viadeo.kasper.core.annotation.XKasperUnregistered;
import com.viadeo.kasper.core.interceptor.InterceptorChain;
import com.viadeo.kasper.cqrs.command.CommandHandler;
import com.viadeo.kasper.cqrs.command.annotation.XKasperCommandHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.viadeo.kasper.context.Context.TAGS_SHORTNAME;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TagsInterceptorUTest {

    static final Context DEFAULT_CONTEXT = Contexts.empty();
    static final Object INPUT = new Object();
    static final Object OUTPUT = new Object();

    //// setup

    @Mock
    InterceptorChain<Object, Object> chain;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public ResetTagsCache resetTagsCache = new ResetTagsCache();

    @Rule
    public ResetMdcContextMap resetMdcContextMap = new ResetMdcContextMap();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    //// process

    @Test
    public void process_WithoutTagsOnTheHandlerAndWhenCallToNextSucceeds_ShouldDoNothing() throws Exception {
        // Given
        @XKasperUnregistered
        @XKasperCommandHandler(domain = TestDomain.class)
        class TestCommandHandler extends CommandHandler<TestCommand> {
        }

        when(chain.next(same(INPUT), eq(DEFAULT_CONTEXT)))
                .thenReturn(OUTPUT);

        // When
        final TagsInterceptor<Object> tagsInterceptor = interceptor(TestCommandHandler.class);
        final Object result = tagsInterceptor.process(INPUT, DEFAULT_CONTEXT, chain);

        // Then
        assertEquals(OUTPUT, result);
    }

    @Test
    public void process_WithoutTagsOnTheHandlerAndWhenCallToNextThrows_ShouldDoNothing() throws Exception {
        // Given
        @XKasperUnregistered
        @XKasperCommandHandler(domain = TestDomain.class)
        class TestCommandHandler extends CommandHandler<TestCommand> {
        }

        // Expect
        final RuntimeException exception = new RuntimeException();
        when(chain.next(same(INPUT), eq(DEFAULT_CONTEXT)))
                .thenThrow(exception);
        thrown.expect(sameInstance(exception));

        // When
        final TagsInterceptor<Object> tagsInterceptor = interceptor(TestCommandHandler.class);
        tagsInterceptor.process(INPUT, DEFAULT_CONTEXT, chain);
    }

    @Test
    public void process_WithTagsOnTheHandler_ShouldAddThemToTheContextAndMdcContextMapForTheNextHandlerInTheChain() throws Exception {
        // Given
        final String tagOnHandler = "this-is-a-tag";
        final String otherTagOnHandler = "this-is-another-tag";

        @XKasperUnregistered
        @XKasperCommandHandler(domain = TestDomain.class, tags = {tagOnHandler, otherTagOnHandler})
        class TestCommandHandler extends CommandHandler<TestCommand> {
        }
        final Set<String> tagsOnHandler = newHashSet(tagOnHandler, otherTagOnHandler);

        // Expect
        when(chain.next(same(INPUT), any(Context.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();
                final Context alteredContext = (Context) arguments[1];

                final Set<String> tagsInContext = alteredContext.getTags();
                assertEquals(tagsOnHandler, tagsInContext);

                final Set<String> tagsInMdcContextMap = getMdcContextMap();
                assertEquals(tagsOnHandler, tagsInMdcContextMap);

                return OUTPUT;
            }
        });

        // When
        final TagsInterceptor<Object> tagsInterceptor = interceptor(TestCommandHandler.class);
        final Object result = tagsInterceptor.process(INPUT, DEFAULT_CONTEXT, chain);

        // Then
        assertEquals(OUTPUT, result);
    }

    @Test
    public void process_WithTagsAlreadyInContext_ShouldAddHandlerTagsToTheExistingOnes() throws Exception {
        // Given
        final String tagOnHandler = "this-is-a-tag";
        final String otherTagOnHandler = "this-is-another-tag";

        @XKasperUnregistered
        @XKasperCommandHandler(domain = TestDomain.class, tags = {tagOnHandler, otherTagOnHandler})
        class TestCommandHandler extends CommandHandler<TestCommand> {
        }
        final Set<String> tagsOnHandler = newHashSet(tagOnHandler, otherTagOnHandler);

        final Set<String> tagsAlreadyInContext = newHashSet("a-tag-already-in-context");
        final Context context = new Context.Builder().withTags(tagsAlreadyInContext).build();

        final Set<String> tagsAlreadyInMdcContextMap = newHashSet("a-tag-already-in-mdc-context-map");
        setMdcContextMap(tagsAlreadyInMdcContextMap);

        // Expect
        when(chain.next(same(INPUT), any(Context.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();
                final Context alteredContext = (Context) arguments[1];

                final Set<String> tagsInContext = alteredContext.getTags();
                final Set<String> expectedTagsInContext = Sets.union(tagsAlreadyInContext, tagsOnHandler);
                assertEquals(expectedTagsInContext, tagsInContext);

                final Set<String> tagsInMdcContextMap = getMdcContextMap();
                final Set<String> expectedTagsInContextMap = Sets.union(tagsAlreadyInMdcContextMap, tagsOnHandler);
                assertEquals(expectedTagsInContextMap, tagsInMdcContextMap);

                return OUTPUT;
            }
        });

        // When
        final TagsInterceptor<Object> tagsInterceptor = interceptor(TestCommandHandler.class);
        final Object result = tagsInterceptor.process(INPUT, context, chain);

        // Then
        assertEquals(OUTPUT, result);
    }

    @Test
    public void process_WithTagsOnTheHandler_ShouldRestoreMdcContextMapAfterExecutionOfNextHandlerInTheChain() throws Exception {
        // Given
        final String tagOnHandler = "this-is-a-tag";
        final String otherTagOnHandler = "this-is-another-tag";

        @XKasperUnregistered
        @XKasperCommandHandler(domain = TestDomain.class, tags = {tagOnHandler, otherTagOnHandler})
        class TestCommandHandler extends CommandHandler<TestCommand> {
        }

        final Set<String> tagsAlreadyInMdcContextMap = newHashSet("a-tag-already-in-mdc-context-map");
        setMdcContextMap(tagsAlreadyInMdcContextMap);

        // When
        final TagsInterceptor<Object> tagsInterceptor = interceptor(TestCommandHandler.class);
        tagsInterceptor.process(INPUT, DEFAULT_CONTEXT, chain);

        // Then
        verify(chain).next(same(INPUT), any(Context.class));
        final Set<String> tagsInMdcContextMap = getMdcContextMap();
        assertEquals(tagsAlreadyInMdcContextMap, tagsInMdcContextMap);
    }

    @Test
    public void process_WithTagsOnTheHandler_ShouldRestoreMdcContextMapAfterExecutionOfNextHandlerInTheChainFails() throws Exception {
        // Given
        final String tagOnHandler = "this-is-a-tag";
        final String otherTagOnHandler = "this-is-another-tag";

        @XKasperUnregistered
        @XKasperCommandHandler(domain = TestDomain.class, tags = {tagOnHandler, otherTagOnHandler})
        class TestCommandHandler extends CommandHandler<TestCommand> {
        }

        final Set<String> tagsAlreadyInMdcContextMap = newHashSet("a-tag-already-in-mdc-context-map");
        setMdcContextMap(tagsAlreadyInMdcContextMap);

        // Expect
        final RuntimeException expectedException = new RuntimeException();
        when(chain.next(same(INPUT), any(Context.class)))
                .thenThrow(expectedException);

        try {
            // When
            final TagsInterceptor<Object> tagsInterceptor = interceptor(TestCommandHandler.class);
            tagsInterceptor.process(INPUT, DEFAULT_CONTEXT, chain);
            fail("should have thrown at this point");
        } catch (Exception e) {
            // Then
            assertSame(expectedException, e);
            final Set<String> tagsInMdcContextMap = getMdcContextMap();
            assertEquals(tagsAlreadyInMdcContextMap, tagsInMdcContextMap);
        }

    }

    // ------------------------------------------------------------------------

    private static TagsInterceptor<Object> interceptor(Class<?> type) {
        return new TagsInterceptor<>(TypeToken.of(type));
    }

    private static Set<String> getMdcContextMap() {
        final String tagsAsString = MDC.get(TAGS_SHORTNAME);
        return Tags.valueOf(tagsAsString);
    }

    private static void setMdcContextMap(Set<String> tagsAlreadyInMdcContextMap) {
        final String tags = Tags.toString(tagsAlreadyInMdcContextMap);
        final Map<String, String> contextMap = ImmutableMap.of(TAGS_SHORTNAME, tags);
        MDC.setContextMap(contextMap);
    }

}
