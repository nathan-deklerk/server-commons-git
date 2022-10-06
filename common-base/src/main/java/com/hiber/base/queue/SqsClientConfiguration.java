package com.hiber.base.queue;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import org.springframework.cloud.aws.core.config.AmazonWebserviceClientFactoryBean;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

/**
 * Provides an instance of {@link AmazonSQSAsyncClient}.
 *
 * The code here is adapted from {@link org.springframework.cloud.aws.messaging.config.annotation.SqsClientConfiguration}, but it
 * does not wrap a client in {@link AmazonSQSBufferedAsyncClient} since buffering and batching offered by that client does not
 * plays nicely with the synchronous mode (see {@link org.springframework.integration.aws.outbound.SqsMessageHandler#setSync(boolean)}.
 * When the synchronous mode is enabled then that buffering client won't have a chance to batch messages and will incur a fixed
 * (200 ms by default) delay per each message.
 */
@Configuration
public class SqsClientConfiguration {
	@Lazy
	@Primary
	@Bean(destroyMethod = "shutdown")
	public AmazonSQSAsyncClient nonBufferedAmazonSqs(
			final AWSCredentialsProvider awsCredentialsProvider,
			final RegionProvider regionProvider
	) throws Exception {
		final AmazonWebserviceClientFactoryBean<AmazonSQSAsyncClient> factoryBean = new AmazonWebserviceClientFactoryBean<>(
				AmazonSQSAsyncClient.class,
				awsCredentialsProvider,
				regionProvider
		);
		factoryBean.afterPropertiesSet();
		return factoryBean.getObject();
	}
}