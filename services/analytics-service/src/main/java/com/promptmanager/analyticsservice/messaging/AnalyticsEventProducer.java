package com.promptmanager.analyticsservice.messaging;

import com.promptmanager.analyticsservice.config.RabbitMQConfig;
import com.promptmanager.analyticsservice.events.PromptViewedEvent;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsEventProducer {

	private final RabbitTemplate rabbitTemplate;

	public AnalyticsEventProducer(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publishViewEvent(PromptViewedEvent event) {

		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.VIEW_ROUTING_KEY, event);

		System.out.println("Published PromptViewedEvent: " + event.getPromptId());
	}
}