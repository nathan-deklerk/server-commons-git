package com.hiber.test;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hiber.base.queue.ConsumerMessageCounter;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hamcrest.Matcher;

import static com.hiber.test.JsonMatcher.aJsonThat;
import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

public interface QueueTester {
	AmazonSQSAsync getAmazonSQS();

	default void purgeQueue(String queueUrl) {
		getAmazonSQS().purgeQueue(new PurgeQueueRequest(queueUrl));

		// According to the SDK documentation purging a queue may take up to 60 seconds, and during purging it is possible to
		// receive messages about to be purged. The code below makes an additional attempt to remove any messages being put in
		// the queue. Note that Localstack behaviour may or may not differ from that of SQS. All in all we try to make make sure
		// that after this method no messages are available in the queue. We cannot guarantee it nevertheless, one of the reason
		// being that reading messages from a queue won't return messages that are currently processed/blocked by the visibility
		// timeout. One other idea is to hook up into message sending, collect identifiers of sent messages and then after a test
		// is finished remove sent messages. But it may be much more involved as I'm not sure right now how hard it can be or even
		// if it is doable.
		while (true) {
			ReceiveMessageRequest receiveMessagesRequest = new ReceiveMessageRequest(queueUrl);
			receiveMessagesRequest.setMaxNumberOfMessages(10);
			receiveMessagesRequest.setWaitTimeSeconds(0);
			receiveMessagesRequest.setVisibilityTimeout(60);

			ReceiveMessageResult receiveMessagesResult = this.getAmazonSQS().receiveMessage(receiveMessagesRequest);

			if (receiveMessagesResult.getMessages().size() == 0)
				break;

			DeleteMessageBatchRequest deleteMessagesRequest = new DeleteMessageBatchRequest(
					queueUrl,
					receiveMessagesResult.getMessages()
							.stream()
							.map(m -> new DeleteMessageBatchRequestEntry(UUID.randomUUID().toString(), m.getReceiptHandle()))
							.collect(Collectors.toList())
			);
			this.getAmazonSQS().deleteMessageBatch(deleteMessagesRequest);
		}
	}

	default void queueSend(String queueName, String messageJson) {
		getAmazonSQS().sendMessage(queueName, messageJson);
	}

	default void queueSend(String queueName, String messageJson, String messageGroupId) {
		SendMessageRequest sendMessageRequest = new SendMessageRequest(queueName, messageJson);
		sendMessageRequest.setMessageGroupId(messageGroupId);
		getAmazonSQS().sendMessage(sendMessageRequest);
	}

	default void queueSend(String queueName, String messageJson, Map<String, String> additionalHeaders) {
		SendMessageRequest sendMessageRequest = new SendMessageRequest(queueName, messageJson);
		additionalHeaders.forEach((key, value) ->
				sendMessageRequest.addMessageAttributesEntry(key, new MessageAttributeValue()
						.withDataType("String")
						.withStringValue(value)
				)
		);
		getAmazonSQS().sendMessageAsync(sendMessageRequest);
	}

	default void verifyQueue(String queueName, String expectedMessage) {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueName);
		receiveMessageRequest.setMaxNumberOfMessages(1);
		receiveMessageRequest.setWaitTimeSeconds(5);
		receiveMessageRequest.setVisibilityTimeout(0);
		ReceiveMessageResult receiveMessageResult = getAmazonSQS().receiveMessage(receiveMessageRequest);
		assertThat(receiveMessageResult.getMessages(), hasSize(1));
		assertThat(receiveMessageResult.getMessages().get(0).getBody(), JsonMatcher.aJsonThat().hasContent(expectedMessage));
	}

	default void verifyQueue(String queueName, String expectedMessage, Map<String, Matcher<String>> expectedHeaders) {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueName);
		receiveMessageRequest.setMaxNumberOfMessages(1);
		receiveMessageRequest.setWaitTimeSeconds(5);
		receiveMessageRequest.setVisibilityTimeout(0);
		receiveMessageRequest.withMessageAttributeNames("All");
		ReceiveMessageResult receiveMessageResult = getAmazonSQS().receiveMessage(receiveMessageRequest);
		assertThat(receiveMessageResult.getMessages(), hasSize(1));
		Message message = receiveMessageResult.getMessages().get(0);
		assertThat(message.getBody(), aJsonThat().hasContent(expectedMessage));
		expectedHeaders.forEach((key, value) -> {
			MessageAttributeValue attributeValue = message.getMessageAttributes().get(key);
			assertThat(attributeValue, notNullValue());
			assertThat(attributeValue.getDataType(), is("String"));
			assertThat(attributeValue.getStringValue(), value);
		});
	}

	default void assertThatQueueIsEmpty(String queueName) {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueName);
		receiveMessageRequest.setWaitTimeSeconds(0);
		receiveMessageRequest.setVisibilityTimeout(0);
		ReceiveMessageResult receiveMessageResult = getAmazonSQS().receiveMessage(receiveMessageRequest);
		assertThat(receiveMessageResult.getMessages(), hasSize(0));
	}

	default void waitForNextMessageConsumed(ConsumerMessageCounter counter, long beforeConsumedCounter) {
		for (int i = 0; i < 500; i++) {
			try {
				sleep(10);
			}
			catch (InterruptedException e) {
				throw new RuntimeException("Sleep interrupted", e);
			}
			if (counter.getCounterValue() > beforeConsumedCounter)
				break;
		}
	}
}