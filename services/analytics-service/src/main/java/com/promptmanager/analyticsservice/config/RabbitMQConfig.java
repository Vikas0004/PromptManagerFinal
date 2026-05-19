package com.promptmanager.analyticsservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String EXCHANGE = "prompt.exchange";

	public static final String ANALYTICS_QUEUE = "analytics.queue";

	public static final String VIEW_ROUTING_KEY = "prompt.viewed";

	@Bean
	public Queue analyticsQueue() {

		return new Queue(ANALYTICS_QUEUE, true);
	}

	@Bean
	public TopicExchange exchange() {

		return new TopicExchange(EXCHANGE);
	}

	@Bean
	public Binding viewBinding(Queue analyticsQueue, TopicExchange exchange) {

		return BindingBuilder.bind(analyticsQueue).to(exchange).with(VIEW_ROUTING_KEY);
	}

	@Bean
	public Jackson2JsonMessageConverter messageConverter() {

		return new Jackson2JsonMessageConverter();
	}
}