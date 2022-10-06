package com.hiber.test.app;

import brave.Span;
import brave.Tracer;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonParseException;
import com.hiber.base.queue.SqsBaseSettings;
import com.hiber.base.queue.SqsQueueConsumersAutoConfiguration.QueueConsumer;
import com.hiber.base.queue.SqsQueueConsumersAutoConfiguration.QueueConsumer.BackoffPolicy;
import com.hiber.test.QueueTester;
import com.hiber.test.sleuth.SpanLogChannelInterceptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.aws.inbound.SqsMessageDrivenChannelAdapter;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static com.hiber.test.app.SqsQueueConsumerTest.TestSqsBaseSettingsValues.EXPONENTIAL_BACKOFF_MIN_VISIBILITY_TIMEOUT;
import static com.hiber.test.app.SqsQueueConsumerTest.TestSqsBaseSettingsValues.TEST_DEFAULT_VISIBILITY_TIMEOUT;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests related to SQS consumers.
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("IntegrationTest")
@Import(SqsQueueConsumerTest.TestConfiguration.class)
@TestPropertySource(properties = {
		"hiber.sqs.wait-time-out=0",
		"hiber.sqs.min-visibility-timeout=5",
		"hiber.sqs.max-visibility-timeout=20"
})
public class SqsQueueConsumerTest implements QueueTester {

	@Autowired
	private AmazonSQSAsync amazonSqs;
	/**
	 * A first test fifo queue consumer defined in the test-specific configuration.
	 */
	@Autowired
	private QueueConsumer<FifoDto> fifoQueueConsumer;
	/**
	 * A first test queue consumer defined in the test-specific configuration.
	 */
	@Autowired
	private QueueConsumer<FirstDto> firstQueueConsumer;
	/**
	 * A second test queue consumer defined in the test-specific configuration.
	 */
	@Autowired
	private QueueConsumer<SecondDto> secondQueueConsumer;
	/**
	 * Brave tracer allowing to get information about current tracing context.
	 */
	@Autowired
	private Tracer tracer;
	/**
	 * A Spring Integration channel that handles all messages from SQS. We use it here to hook-up various interceptors.
	 */
	@Autowired
	private DirectChannel rawReceiveMessageChannel;
	/**
	 * An extra notifier that allows to check exception thrown during message processing.
	 */
	@Autowired
	private ExceptionRecordingSupportNotifier exceptionRecordingSupportNotifier;

	@BeforeEach
	public void clearQueues() {
		purgeQueue(fifoQueueConsumer.getUrl());
		purgeQueue(firstQueueConsumer.getUrl());
		purgeQueue(secondQueueConsumer.getUrl());
	}

	@Override
	public AmazonSQSAsync getAmazonSQS() {
		return amazonSqs;
	}

	@AfterEach
	public void resetRecorders() {
		exceptionRecordingSupportNotifier.clear();
	}

	/**
	 * Tests that when an object constructed from a JSON payload fails some validation constraints configured support notifiers
	 * will receive an appropriate exception.
	 */
	@Test
	public void shouldForwardExceptionDuringValidationToConfiguredSupportNotifiers() {
		final ExceptionThrowingConsumer consumer = new ExceptionThrowingConsumer();
		((MutableConsumer<FirstDto>) firstQueueConsumer.getConsumer()).setConsumer(consumer);

		amazonSqs.sendMessage(firstQueueConsumer.getUrl(), "{}");
		await()
				.atMost(5, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 1);

		final Throwable validationException = exceptionRecordingSupportNotifier.getExceptions().get(0).getCause().getCause();
		assertThat(validationException.getClass(), is(ConstraintViolationException.class));
	}

	/**
	 * Tests that when an object constructed from a JSON payload fails some validation constraints configured support notifiers
	 * will receive an appropriate exception.
	 */
	@Test
	public void shouldForwardExceptionDuringValidationToConfiguredSupportNotifiers_forFifoQueue() {
		final ExceptionThrowingFifoConsumer consumer = new ExceptionThrowingFifoConsumer();
		((MutableConsumer<FifoDto>) fifoQueueConsumer.getConsumer()).setConsumer(consumer);

		final SendMessageRequest messageRequest = new SendMessageRequest()
				.withQueueUrl(fifoQueueConsumer.getUrl())
				.withMessageBody("{}")
				.withMessageGroupId("messageGroupId");

		amazonSqs.sendMessage(messageRequest);
		await()
				.atMost(5, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 1);

		final Throwable validationException = exceptionRecordingSupportNotifier.getExceptions().get(0).getCause().getCause();
		assertThat(validationException.getClass(), is(ConstraintViolationException.class));
	}

	/**
	 * Tests that when a consumer throws an exception configured support notifiers will receive it.
	 */
	@Test
	public void shouldForwardExceptionInConsumerToConfiguredSupportNotifiers() {
		final ExceptionThrowingConsumer consumer = new ExceptionThrowingConsumer();
		((MutableConsumer<FirstDto>) firstQueueConsumer.getConsumer()).setConsumer(consumer);

		queueSend(firstQueueConsumer.getUrl(), "{\"text\": \"Ho!\", \"count\": 9}");
		await()
				.atMost(1, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 1);

		final Throwable consumerException = exceptionRecordingSupportNotifier.getExceptions().get(0).getCause().getCause();
		assertThat(
				consumerException.getMessage(),
				is("An exception while processing SqsQueueConsumerTest.FirstDto(text=Ho!, count=9)")
		);
	}

	/**
	 * Tests that when an object cannot be de-serialized from a JSON payload then configured support notifiers will receive an
	 * appropriate exception.
	 */
	@Test
	public void shouldForwardExceptionWhenDeserializingToConfiguredSupportNotifiers() {
		final ExceptionThrowingConsumer consumer = new ExceptionThrowingConsumer();
		((MutableConsumer<FirstDto>) firstQueueConsumer.getConsumer()).setConsumer(consumer);

		amazonSqs.sendMessage(firstQueueConsumer.getUrl(), "<nope><i/><am/><not/><json/></nope>");
		await()
				.atMost(5, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 1);

		final Throwable parsingException = exceptionRecordingSupportNotifier.getExceptions().get(0).getCause().getCause();
		assertThat(parsingException.getClass(), is(JsonParseException.class));
	}

	/**
	 * Tests that when an object cannot be de-serialized from a JSON payload then configured support notifiers will receive an
	 * appropriate exception.
	 */
	@Test
	public void shouldForwardExceptionWhenDeserializingToConfiguredSupportNotifiers_forFifoQueue() {
		final ExceptionThrowingFifoConsumer consumer = new ExceptionThrowingFifoConsumer();
		((MutableConsumer<FifoDto>) fifoQueueConsumer.getConsumer()).setConsumer(consumer);

		final SendMessageRequest messageRequest = new SendMessageRequest()
				.withQueueUrl(fifoQueueConsumer.getUrl())
				.withMessageBody("<nope><i/><am/><not/><json/></nope>")
				.withMessageGroupId("messageGroupId");

		amazonSqs.sendMessage(messageRequest);
		await()
				.atMost(5, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 1);

		final Throwable parsingException = exceptionRecordingSupportNotifier.getExceptions().get(0).getCause().getCause();
		assertThat(parsingException.getClass(), is(JsonParseException.class));
	}

	/**
	 * Test that check visibility timeout between attempts of consuming message.
	 *
	 * Note : Awaitility sometimes throw ConditionTimeoutException on atLeast setting:
	 * 'Condition was evaluated in 14 seconds 939 milliseconds which is earlier than expected minimum timeout 15 seconds'
	 *
	 * Probably this time (15 - 14.939 milliseconds) is needed for setting up timer
	 * so precise value of visibility timeout cant be measured.
	 */
	@Test
	public void shouldIncreaseVisibilityTimeoutWhenExceptionOccurred() {
		final TransientExceptionConsumer<FirstDto> consumer = new TransientExceptionConsumer<>();
		((MutableConsumer<FirstDto>) firstQueueConsumer.getConsumer()).setConsumer(consumer);

		queueSend(firstQueueConsumer.getUrl(), "{\"text\": \"Hello!\", \"count\": 100}");

		final int firstVisibilityTimeout = 5;
		final int secondVisibilityTimeout = 10;
		final int thirdVisibilityTimeout = 20;
		await()
				.atMost(1, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 1);
		await()
				.atLeast(firstVisibilityTimeout - 1, TimeUnit.SECONDS)
				.atMost(firstVisibilityTimeout + 1, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 2);
		await()
				.atLeast(secondVisibilityTimeout - 1, TimeUnit.SECONDS)
				.atMost(secondVisibilityTimeout + 1, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 3);

		await()
				.atLeast(thirdVisibilityTimeout - 1, TimeUnit.SECONDS)
				.atMost(thirdVisibilityTimeout + 1, TimeUnit.SECONDS)
				.until(consumer::isMessageSuccessfulConsumed);

		assertThat(consumer.getMessage(), is(new FirstDto("Hello!", 100)));
	}

	/**
	 * Test that we get an exception from Amazon SQS when we do not provide message group id when using a fifo.
	 */
	@Test
	public void shouldThrowExceptionIfMissingMessageGroupIdForFifoQueue() {
		final ExceptionThrowingFifoConsumer consumer = new ExceptionThrowingFifoConsumer();
		((MutableConsumer<FifoDto>) fifoQueueConsumer.getConsumer()).setConsumer(consumer);

		final SendMessageRequest messageRequest = new SendMessageRequest()
				.withQueueUrl(fifoQueueConsumer.getUrl())
				.withMessageBody("<nope><i/><am/><not/><json/></nope>");

		AmazonSQSException exception = assertThrows(
				AmazonSQSException.class,
				() -> amazonSqs.sendMessage(messageRequest)
		);
		assertThat(
				exception.getMessage(),
				is("InvalidParameterValue; see the SQS docs. (Service: AmazonSQS; Status Code: 400; Error Code: InvalidParameterValue; Request ID: 00000000-0000-0000-0000-000000000000)")
		);
	}

	/**
	 * Tests that all existing QueueConsumer instances will still work
	 * with default VisibilityTimeout parameter and use CONFIGURED(configured by AWS) back-off policy.
	 */
	@Test
	public void visibilityTimeoutShouldStayDefault_forExistingQueueConsumers() {
		final TransientExceptionConsumer<SecondDto> consumer = new TransientExceptionConsumer<>();
		((MutableConsumer<SecondDto>) secondQueueConsumer.getConsumer()).setConsumer(consumer);

		queueSend(secondQueueConsumer.getUrl(), "{\"name\": \"Foobar\", \"active\": false}");

		await()
				.atMost(1, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 1);

		await()
				.atLeast(TEST_DEFAULT_VISIBILITY_TIMEOUT - 1, TimeUnit.SECONDS)
				.atMost(TEST_DEFAULT_VISIBILITY_TIMEOUT + 1, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 2);

		await()
				.atLeast(TEST_DEFAULT_VISIBILITY_TIMEOUT - 1, TimeUnit.SECONDS)
				.atMost(TEST_DEFAULT_VISIBILITY_TIMEOUT + 1, TimeUnit.SECONDS)
				.until(() -> exceptionRecordingSupportNotifier.getExceptions().size() == 3);

		await()
				.atLeast(TEST_DEFAULT_VISIBILITY_TIMEOUT - 1, TimeUnit.SECONDS)
				.atMost(TEST_DEFAULT_VISIBILITY_TIMEOUT + 1, TimeUnit.SECONDS)
				.until(consumer::isMessageSuccessfulConsumed);

		assertThat(consumer.getMessage(), is(new SecondDto("Foobar", false)));
	}

	/**
	 * Tests that when tracing identifiers (trace, span) are present in the headers of a message received from a SQS queue they
	 * are used to set up and propagate tracing context.
	 */
	@Test
	void shouldExtractTracingIdentifiersFromHeadersAndSetTracingContext() {
		final SpanLogChannelInterceptor interceptor = new SpanLogChannelInterceptor(tracer);
		rawReceiveMessageChannel.addInterceptor(interceptor);

		queueSend(firstQueueConsumer.getUrl(), "{\"text\": \"\"}", Map.of(
				"X-B3-TraceId",
				"d732ffcf5b37f521",
				"X-B3-SpanId",
				"d732ffcf5b37f522"
		));
		await()
				.atMost(5, TimeUnit.SECONDS)
				.until(() -> interceptor.getLoggedSpans().size() == 1);

		final Span loggedSpan = interceptor.getLoggedSpans().get(0);
		assertThat(loggedSpan.context().traceIdString(), is("d732ffcf5b37f521"));
		// We can't really test other tracing properties like parent span as when a message flows through defined Spring Integration
		// components there will be many spans created.
	}

	/**
	 * Tests that a valid message received from a queue is forwarded to configured consumers.
	 */
	@Test
	void shouldForwardIncomingMessageToAppropriateConsumers() {
		final MessageRecordingConsumer<FifoDto> fifoConsumer = new MessageRecordingConsumer<>();
		((MutableConsumer<FifoDto>) fifoQueueConsumer.getConsumer()).setConsumer(fifoConsumer);
		final MessageRecordingConsumer<FirstDto> firstConsumer = new MessageRecordingConsumer<>();
		((MutableConsumer<FirstDto>) firstQueueConsumer.getConsumer()).setConsumer(firstConsumer);
		final MessageRecordingConsumer<SecondDto> secondConsumer = new MessageRecordingConsumer<>();
		((MutableConsumer<SecondDto>) secondQueueConsumer.getConsumer()).setConsumer(secondConsumer);

		queueSend(fifoQueueConsumer.getUrl(), "{\"text\": \"Hello!\", \"active\": true}", "messageGroupId");
		queueSend(firstQueueConsumer.getUrl(), "{\"text\": \"Hello!\", \"count\": 100}");
		queueSend(secondQueueConsumer.getUrl(), "{\"name\": \"Foobar\", \"active\": false}");
		await()
				.atMost(5, TimeUnit.SECONDS)
				.until(() -> fifoConsumer.messages.size() == 1
						&& firstConsumer.messages.size() == 1
						&& secondConsumer.messages.size() == 1
				);

		assertThat(fifoConsumer.messages.get(0), is(new FifoDto("Hello!", true)));
		assertThat(firstConsumer.messages.get(0), is(new FirstDto("Hello!", 100)));
		assertThat(secondConsumer.messages.get(0), is(new SecondDto("Foobar", false)));
	}

	interface TestSqsBaseSettingsValues {
		/**
		 * Min Visibility timeout Exponential value for tests.
		 */
		int EXPONENTIAL_BACKOFF_MIN_VISIBILITY_TIMEOUT = 5;

		/**
		 * Default visibility timeout for tests.
		 */
		int TEST_DEFAULT_VISIBILITY_TIMEOUT = 5;
	}

	/**
	 * Test-specific configuration that sets up a queue consumer and other classes supporting tests.
	 */
	@org.springframework.boot.test.context.TestConfiguration
	public static class TestConfiguration {
		@Bean
		public ExceptionRecordingSupportNotifier exceptionRecordingSupportNotifier() {
			return new ExceptionRecordingSupportNotifier();
		}

		@Bean
		public QueueConsumer<FifoDto> fifoQueueConsumer(final SqsSettings sqsSettings) {
			return new QueueConsumer<>(
					sqsSettings.getFifoQueueName(),
					new MutableConsumer(),
					FifoDto.class,
					"fifo-queue"
			);
		}

		@Bean
		public QueueConsumer<FirstDto> firstQueueConsumer(final SqsSettings sqsSettings) {
			return new QueueConsumer<>(
					sqsSettings.getFirstQueueName(),
					new MutableConsumer(),
					FirstDto.class,
					"first-queue",
					BackoffPolicy.EXPONENTIAL
			);
		}

		@Bean
		public QueueConsumer<SecondDto> secondQueueConsumer(final SqsSettings sqsSettings) {
			return new QueueConsumer<>(
					sqsSettings.getSecondQueueName(),
					new MutableConsumer(),
					SecondDto.class,
					"second-queue"
			);
		}

		@Bean
		public SqsBaseSettings sqsBaseSettings() {
			return new SqsBaseSettings(
					20,
					43200,
					EXPONENTIAL_BACKOFF_MIN_VISIBILITY_TIMEOUT
			);
		}

		/**
		 * Overriding default visibility timeout (by default 30s).
		 */
		@Bean
		public MessageProducerSupport sqsTestMessageProducerSupport(final MessageProducerSupport sqsMessageProducerSupport) {
			((SqsMessageDrivenChannelAdapter) sqsMessageProducerSupport)
					.setVisibilityTimeout(TEST_DEFAULT_VISIBILITY_TIMEOUT);

			return sqsMessageProducerSupport;
		}
	}

	/**
	 * A consumer that allows to swap underlying implementation.
	 */
	@Data
	static class MutableConsumer<T> implements Consumer<T> {
		private Consumer<T> consumer = new NoopConsumer();

		@Override
		public void accept(final T firstDto) {
			consumer.accept(firstDto);
		}
	}

	static class NoopConsumer<T> implements Consumer<T> {
		@Override
		public void accept(T firstDto) {
		}
	}

	static class MessageRecordingConsumer<T> implements Consumer<T> {
		private final List<T> messages = new ArrayList<>();

		@Override
		public void accept(final T dto) {
			messages.add(dto);
		}
	}

	static class ExceptionThrowingConsumer implements Consumer<FirstDto> {
		@Override
		public void accept(FirstDto firstDto) {
			throw new RuntimeException("An exception while processing " + firstDto);
		}
	}

	static class ExceptionThrowingFifoConsumer implements Consumer<FifoDto> {
		@Override
		public void accept(FifoDto fifoDto) {
			throw new RuntimeException("An exception while processing " + fifoDto);
		}
	}

	/**
	 * Class helpful in testing message consumer behaviour
	 * both visibility timeout and message data.
	 *
	 * 1st, 2nd and 3rd call of accept(T t) method throw exception.
	 * (It simulates fail on consumer side)
	 * From the 4h call add(T t) method exception isn't throw and
	 * passed argument t is set to veriable.
	 *
	 * @param <T> Type of message consumed.
	 */
	static class TransientExceptionConsumer<T> implements Consumer<T> {
		private Optional<T> data = Optional.empty();
		private int exceptionCounter = 3;

		@Override
		public void accept(T t) {
			if (exceptionCounter > 0) {
				exceptionCounter--;
				throw new RuntimeException();
			}

			data = Optional.ofNullable(t);
		}

		public T getMessage() {
			return data.get();
		}

		public boolean isMessageSuccessfulConsumed() {
			return data.isPresent();
		}
	}

	/**
	 * Represents messages exchanged via the third test SQS fifo queue for the purpose of these tests.
	 */
	@Value
	public static class FifoDto {
		@NotNull
		String text;

		boolean active;
	}

	/**
	 * Represents messages exchanged via the first test SQS queue for the purpose of these tests.
	 */
	@Value
	public static class FirstDto {
		@NotNull
		String text;

		int count;
	}

	/**
	 * Represents messages exchanged via the second test SQS queue for the purpose of these tests.
	 */
	@Value
	public static class SecondDto {
		@NotNull
		String name;

		boolean active;
	}
}
