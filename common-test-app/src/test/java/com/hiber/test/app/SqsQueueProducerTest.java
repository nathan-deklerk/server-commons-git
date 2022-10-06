package com.hiber.test.app;

import brave.Span;
import brave.Tracer;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.hiber.base.queue.SqsProducerFactory;
import com.hiber.test.QueueTester;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Value;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static com.hiber.test.utils.JsonTestUtils.json;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests related to SQS producers.
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("IntegrationTest")
@Import(SqsQueueProducerTest.TestConfiguration.class)
@TestPropertySource(properties = {
		"hiber.sqs.consumer-enabled=false"
})
class SqsQueueProducerTest implements QueueTester {
	@Autowired
	private AmazonSQSAsync amazonSqs;

	@Autowired
	private MessageChannel testProducerChannel;

	@Autowired
	private MessageChannel testProducerFifoChannel;

	@Autowired
	private MessageChannel invalidProducerChannel;

	@Autowired
	private SqsSettings sqsSettings;

	@Autowired
	private Tracer tracer;

	@Override
	public AmazonSQSAsync getAmazonSQS() {
		return amazonSqs;
	}

	@BeforeEach
	public void clearQueues() {
		purgeQueue(sqsSettings.getFifoQueueName());
		purgeQueue(sqsSettings.getFirstQueueName());
	}

	/**
	 * Tests that a producer channel created using the factory sends properly a message to an SQS queue.
	 */
	@Test
	void shouldSendMessage() {
		testProducerChannel.send(MessageBuilder.withPayload(new ExampleDto("Hello!")).build());
		testProducerFifoChannel.send(MessageBuilder.withPayload(new ExampleDto("Hello!")).build());

		verifyQueue(sqsSettings.getFifoQueueName(), "{\"text\": \"Hello!\"}");
		verifyQueue(sqsSettings.getFirstQueueName(), "{\"text\": \"Hello!\"}");
	}

	/**
	 * Tests that adding fifo attributes to queue.
	 */
	@Test
	void shouldAddFifoQueueAttributes() {
		final Map<String, String> attributes = getAmazonSQS().getQueueAttributes(
				sqsSettings.getFifoQueueName(),
				List.of("FifoQueue", "ContentBasedDeduplication")
		).getAttributes();
		assertThat(attributes, hasEntry(is("FifoQueue"), is("true")));
		assertThat(attributes, hasEntry(is("ContentBasedDeduplication"), is("true")));
	}

	/**
	 * Tests that tracing identifiers will be added to a sent message.
	 */
	@Test
	void shouldAddTracingIdentifiers() {
		final Span span = tracer.nextSpan();

		try (Tracer.SpanInScope ignored = this.tracer.withSpanInScope(span.start())) {
			testProducerChannel.send(MessageBuilder.withPayload(new ExampleDto("Surprise!")).build());
		}
		finally {
			span.finish();
		}

		Map<String, Matcher<String>> expectedHeaders = new HashMap<>();
		expectedHeaders.put("X-B3-SpanId", matchesPattern(".*"));
		expectedHeaders.put("X-B3-TraceId", is(span.context().traceIdString()));
		verifyQueue(sqsSettings.getFirstQueueName(), json("{\"text\": \"Surprise!\"}"), expectedHeaders);
	}

	/**
	 * Tests that an exception caused by non-serializable message is propagated to the place when a message is sent.
	 */
	@Test
	void shouldPropagateExceptionDuringSerialisationToJson() {
		assertThrows(
				MessageTransformationException.class,
				() -> testProducerChannel.send(MessageBuilder.withPayload(new NonSerializableDto()).build())
		);

		assertThrows(
				MessageTransformationException.class,
				() -> testProducerFifoChannel.send(MessageBuilder.withPayload(new NonSerializableDto()).build())
		);
	}

	/**
	 * Tests that an exception when sending a message to a SQS queue is propagated to the place when a message is sent.
	 */
	@Test
	void shouldPropagateExceptionDuringSendingMessage() {
		assertThrows(
				MessageHandlingException.class,
				() -> invalidProducerChannel.send(MessageBuilder.withPayload(new ExampleDto("Ouch!")).build())
		);
	}

	/**
	 * Tests that non-buffered SQS client is used. Built-in SQS auto-configuration will still create an instance of
	 * AmazonSQSBufferedAsyncClient but this test will hopefully catch any problems in case of updating to newer Spring-related
	 * dependencies.
	 */
	@Test
	void shouldNotUseBufferedSqsClient() {
		final AmazonSQSAsync amazonSQS = getAmazonSQS();

		assertThat(amazonSQS, not(instanceOf(AmazonSQSBufferedAsyncClient.class)));
	}

	/**
	 * Test-specific configuration that sets up producer channels.
	 */
	@org.springframework.boot.test.context.TestConfiguration
	public static class TestConfiguration {
		@Bean
		public MessageChannel testProducerChannel(final SqsProducerFactory sqsProducerFactory, final SqsSettings sqsSettings) {
			return sqsProducerFactory.create(sqsSettings.getFirstQueueName());
		}

		@Bean
		public MessageChannel testProducerFifoChannel(final SqsProducerFactory sqsProducerFactory, final SqsSettings sqsSettings) {
			return sqsProducerFactory.create(sqsSettings.getFifoQueueName(), "message-group-id");
		}

		@Bean
		public MessageChannel invalidProducerChannel(final SqsProducerFactory sqsProducerFactory) {
			return sqsProducerFactory.create("http://localhost:9999/queue/missing");
		}
	}

	/**
	 * Represents messages exchanged via a test SQS queue for the purpose of these tests.
	 */
	@Value
	public static class ExampleDto {
		String text;
	}

	/**
	 * Represents a message that could not be serialized.
	 */
	@Value
	public static class NonSerializableDto {
		Object o = Thread.currentThread(); // Happens to be non-serializable by Jackson, unlikely to change in future versions.
	}
}