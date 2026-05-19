package com.promptmanager.analyticsservice.messaging;

import com.promptmanager.analyticsservice.config.RabbitMQConfig;
import com.promptmanager.analyticsservice.events.PromptViewedEvent;
import com.promptmanager.analyticsservice.service.AnalyticsService;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsEventConsumer {

	private final AnalyticsService analyticsService;

	public AnalyticsEventConsumer(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@RabbitListener(queues = RabbitMQConfig.ANALYTICS_QUEUE)
	public void consumeViewEvent(PromptViewedEvent event) {

		System.out.println("Consumed PromptViewedEvent: " + event.getPromptId());

		analyticsService.recordView(event.getPromptId(), event.getUsername());
	}
}