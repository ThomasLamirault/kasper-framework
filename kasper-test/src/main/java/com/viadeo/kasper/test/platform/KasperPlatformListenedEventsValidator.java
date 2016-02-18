// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.test.platform;

import com.google.common.collect.Lists;
import com.viadeo.kasper.api.component.command.Command;
import com.viadeo.kasper.api.component.event.Event;
import com.viadeo.kasper.core.component.event.listener.EventListener;
import com.viadeo.kasper.core.component.event.listener.EventMessage;
import com.viadeo.kasper.test.platform.validator.KasperFixtureEventResultValidator;
import com.viadeo.kasper.test.platform.validator.base.DefaultBaseValidator;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;

import static com.viadeo.kasper.test.platform.KasperMatcher.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class KasperPlatformListenedEventsValidator
        extends DefaultBaseValidator
        implements KasperFixtureEventResultValidator {

    KasperPlatformListenedEventsValidator(
            final KasperPlatformFixture.RecordingPlatform platform,
            final Exception exception) {
        super(platform, null, exception);
    }

    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public KasperFixtureEventResultValidator expectEventNotificationOn(final Class... listenerClasses) {
        final Set<Class<? extends EventListener>> remainingListenerClasses = platform().listeners.keySet();

        for (final Class listenerClass : listenerClasses) {
            final EventListener eventListener = platform().listeners.get(listenerClass);
            assertNotNull("Unknown event listener : " + listenerClass.getName(), eventListener);

            final ArgumentCaptor<EventMessage> captor = ArgumentCaptor.forClass(EventMessage.class);
            verify(eventListener).handle(captor.capture());

            final List<Event> events = Lists.newArrayList();
            for (final EventMessage eventMessage : captor.getAllValues()) {
                events.add(eventMessage.getEvent());
            }

            assertEquals(platform().getRecordedEvents(eventListener.getInputClass()), events);

            remainingListenerClasses.remove(listenerClass);
        }

        for (final Class listenerClass : remainingListenerClasses) {
            verify(platform().listeners.get(listenerClass), never()).handle(any(EventMessage.class));
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public KasperFixtureEventResultValidator expectZeroEventNotification() {
        for (final EventListener eventListener : platform().listeners.values()) {
            verify(eventListener, never()).handle(any(EventMessage.class));
        }
        return this;
    }

    @Override
    public KasperFixtureEventResultValidator expectExactSequenceOfCommands(final Command... commands) {
        final List<Command> actualCommands = platform().recordedCommands;
        assertEquals(commands.length, actualCommands.size());

        for (int i = 0; i < commands.length; i++) {
            assertTrue(equalTo(commands[i]).matches(actualCommands.get(i)));
        }
        return this;
    }

}
