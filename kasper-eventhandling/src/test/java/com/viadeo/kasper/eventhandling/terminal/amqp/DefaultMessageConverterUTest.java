// ============================================================================
//                 KASPER - Kasper is the treasure keeper
//    www.viadeo.com - mobile.viadeo.com - api.viadeo.com - dev.viadeo.com
//
//           Viadeo Framework for effective CQRS/DDD architecture
// ============================================================================
package com.viadeo.kasper.eventhandling.terminal.amqp;

import com.google.common.collect.ImmutableMap;
import com.viadeo.kasper.context.Context;
import org.axonframework.domain.GenericDomainEventMessage;
import org.axonframework.domain.GenericEventMessage;
import org.axonframework.serializer.SerializedObject;
import org.axonframework.serializer.Serializer;
import org.axonframework.serializer.SimpleSerializedObject;
import org.axonframework.serializer.SimpleSerializedType;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.util.HashMap;
import java.util.Map;

import static com.viadeo.kasper.eventhandling.terminal.amqp.EventMessageConverter.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMessageConverterUTest {

    @Mock
    private Serializer serializer;


    private EventMessageConverter converter;
    private DateTime timestamp;
    private Map<String, Object> properties;

    @Before
    public void setUp() throws Exception {
        converter = new EventMessageConverter(serializer);
        timestamp = new DateTime("2012-10-12");
        properties = ImmutableMap.<String, Object>builder()
                .put("foo", "bar")
                .build();
    }

    @Test
    public void toMessage_withDomainEventMessage_isOk() throws Exception {
        // Given
        SimpleSerializedObject<byte[]> serializedObject = new SimpleSerializedObject<>("payload".getBytes(), byte[].class, new SimpleSerializedType("bytes", "payload-revision"));
        when(serializer.serialize(anyObject(), any(Class.class))).thenReturn(serializedObject);

        GenericDomainEventMessage<String> axonMessage = new GenericDomainEventMessage<>("event-id", timestamp, "event-aggregate-id", 1L, "payload", properties);

        // When
        Message springMessage = converter.toMessage(axonMessage, new MessageProperties());

        // Then
        assertNotNull(springMessage);
        assertEquals("payload", new String(springMessage.getBody()));

        MessageProperties actualProperties = springMessage.getMessageProperties();
        assertEquals(MessageDeliveryMode.PERSISTENT, actualProperties.getDeliveryMode());
        assertEquals("event-id", actualProperties.getMessageId());
        assertEquals("application/json", actualProperties.getContentType());
        assertEquals("UTF-8", actualProperties.getContentEncoding());
        assertEquals("java.lang.String", actualProperties.getType());

        Map<String, Object> headers = actualProperties.getHeaders();
        assertTrue(headers.containsKey(PAYLOAD_REVISION_KEY));
        assertTrue(headers.containsKey(PAYLOAD_TYPE_KEY));
        assertEquals("1.0", headers.get(SERIALIZER_VERSION_KEY));
        assertEquals("2012-10-12T00:00:00.000+02:00", headers.get(EVENT_TIMESTAMP_KEY));
        assertEquals("event-aggregate-id", headers.get(AGGREGATE_ID_KEY));
        assertEquals(1L, headers.get(SEQUENCE_NUMBER_KEY));
        assertEquals((byte) 3, headers.get(EVENT_TYPE_KEY));
        assertEquals("payload-revision", headers.get(PAYLOAD_REVISION_KEY));
        assertEquals("bar", headers.get(PREFIX_METADATA_KEY + "foo"));
    }

    @Test
    public void toMessage_withEventMessage_isOk() throws Exception {
        // Given
        SimpleSerializedObject<byte[]> serializedObject = new SimpleSerializedObject<>("payload".getBytes(), byte[].class, new SimpleSerializedType("bytes", "payload-revision"));
        when(serializer.serialize(anyObject(), any(Class.class))).thenReturn(serializedObject);

        GenericEventMessage<String> axonMessage = new GenericEventMessage<>("event-id", timestamp, "payload", properties);

        // When
        Message springMessage = converter.toMessage(axonMessage, new MessageProperties());

        // Then
        assertNotNull(springMessage);

        Map<String, Object> headers = springMessage.getMessageProperties().getHeaders();
        assertEquals((byte) 1, headers.get(EVENT_TYPE_KEY));
        assertFalse(headers.containsKey(AGGREGATE_ID_KEY));
        assertFalse(headers.containsKey(SEQUENCE_NUMBER_KEY));
    }

    @Test(expected = NullPointerException.class)
    public void toMessage_withNullAsEventMessage_throwException() throws Exception {
        // Given nothing
        // When
        converter.toMessage(null, new MessageProperties());
        // Then throw exception
    }

    @Test
    public void toMessage_withNullAsValueOfContextProperties_isOk() {
        // Given
        properties = new HashMap<>(properties);
        properties.put("lolilou", null);

        final SimpleSerializedObject<byte[]> serializedObject = new SimpleSerializedObject<>("payload".getBytes(), byte[].class, new SimpleSerializedType("bytes", "payload-revision"));
        when(serializer.serialize(anyObject(), any(Class.class))).thenReturn(serializedObject);

        final GenericEventMessage<String> axonMessage = new GenericEventMessage<>("event-id", timestamp, "payload", properties);

        // When
        converter.toMessage(axonMessage, new MessageProperties());

        // Then no exception is thrown
    }

    @Test
    public void fromMessage_fromDomainEventMessage_isOk() throws Exception {
        // Given
        final String payload = "foobar";
        when(serializer.deserialize(any(SerializedObject.class))).thenReturn(payload);
        Message amqpMessage = MessageBuilder.withBody(payload.getBytes())
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                .setContentEncoding("charset=utf-8")
                .setContentType("application/json")
                .setType("java.lang.String")
                .setHeader(AGGREGATE_ID_KEY, "aggregate-id")
                .setHeader(PAYLOAD_TYPE_KEY, payload.getClass().getName())
                .setHeader(PAYLOAD_REVISION_KEY, "0")
                .setHeader(SEQUENCE_NUMBER_KEY, 1L)
                .setHeader(EVENT_TYPE_KEY, "")
                .setHeader(EVENT_TIMESTAMP_KEY, "2012-10-12T00:00:00.000+02:00")
                .setHeader(SERIALIZER_VERSION_KEY, "1.0")
                .build();


        // When
        final GenericDomainEventMessage eventMessage = (GenericDomainEventMessage) converter.fromMessage(amqpMessage);

        // Then
        assertNotNull(eventMessage);
        assertEquals(payload, eventMessage.getPayload());
        assertEquals(payload.getClass(), eventMessage.getPayloadType());
        assertEquals(1L, eventMessage.getSequenceNumber());
        assertEquals("aggregate-id", eventMessage.getAggregateIdentifier());

    }

    @Test
    public void fromMessage_fromEventMessage_isOk() throws Exception {
        // Given
        final String payload = "toto";

        when(serializer.deserialize(any(SerializedObject.class))).thenReturn(payload);

        Message amqpMessage = MessageBuilder.withBody(payload.getBytes())
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                .setContentEncoding("charset=utf-8")
                .setContentType("application/json")
                .setType("java.lang.String")
                .setHeader(AGGREGATE_ID_KEY, "aggregate-id")
                .setHeader(PAYLOAD_TYPE_KEY, payload.getClass().getName())
                .setHeader(PAYLOAD_REVISION_KEY, "0")
                .setHeader(SEQUENCE_NUMBER_KEY, 1L)
                .setHeader(EVENT_TYPE_KEY, "")
                .setHeader(EVENT_TIMESTAMP_KEY, "2012-10-12T00:00:00.000+02:00")
                .setHeader(SERIALIZER_VERSION_KEY, "1.0")
                .build();


        // When
        final GenericDomainEventMessage eventMessage = (GenericDomainEventMessage) converter.fromMessage(amqpMessage);

        // Then
        assertNotNull(eventMessage);
        assertEquals(payload, eventMessage.getPayload());
        assertEquals(payload.getClass(), eventMessage.getPayloadType());
    }

    @Test(expected = MessageConversionException.class)
    public void readAMQPMessage_withNullAsByteArray_throwException() throws Exception {
        // Given nothing
        // When
        converter.fromMessage(null);
        // Then throw exception
    }

    @Test(expected = MessageConversionException.class)
    public void readAMQPMessage_withNullAsProperties_throwException() throws Exception {
        // Given nothing
        Message amqpMessage = MessageBuilder.withBody("boo".getBytes()).build();

        // When
        converter.fromMessage(amqpMessage);
        // Then throw exception
    }

    @Test(expected = NullPointerException.class)
    public void toMetadata_withNullAsProperties_throwException() {
        // Given nothing
        // When
        converter.toMetadata(null);
        // Then throw exception
    }

    @Test()
    public void toMetadata_withMap_withHeaders_throwException() {
        // Given

        MessageProperties properties = new MessageProperties();

        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setContentEncoding("charset=utf-8");
        properties.setContentType("application/json");
        properties.setType("java.lang.String");
        properties.setHeader(PREFIX_CONTEXT_KEY + Context.ULANG_SHORTNAME, "fr");


        // When
        final Map<String, ?> metadata = converter.toMetadata(properties);

        // Then
        assertNotNull(metadata);
        assertEquals(2, metadata.get("delivery-mode"));
        assertEquals("charset=utf-8", metadata.get("content-encoding"));
        assertEquals("application/json", metadata.get("content-type"));
        assertEquals("java.lang.String", metadata.get("type"));

        final Object object = metadata.get(Context.METANAME);
        assertNotNull(object);
        assertTrue(object instanceof Context);
        assertEquals("fr", ((Context) object).getUserLang());
    }

    @Test()
    public void toMetadata_withMap_withoutHeaders_throwException() {

        // Given
        MessageProperties properties = new MessageProperties();

        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setContentEncoding("charset=utf-8");
        properties.setContentType("application/json");
        properties.setType("java.lang.String");

        // When
        final Map<String, ?> metadata = converter.toMetadata(properties);

        // Then
        assertNotNull(metadata);
        assertEquals(2, metadata.get("delivery-mode"));
        assertEquals("charset=utf-8", metadata.get("content-encoding"));
        assertEquals("application/json", metadata.get("content-type"));
        assertEquals("java.lang.String", metadata.get("type"));
    }
}