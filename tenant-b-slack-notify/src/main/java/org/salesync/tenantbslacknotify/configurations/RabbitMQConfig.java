package org.salesync.tenantbslacknotify.configurations;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue tenantBSlackNotifyQueue() {
        return new Queue("tenant-b-slack-notify-queue", true, false, false);
    }

    @Bean
    public Queue tenantBSlackNotifySettingQueue() {
        return new Queue("tenant-b-slack-notify-setting-queue", true, false, false);
    }

    @Bean
    public Exchange exchange() {
        return new TopicExchange("topic-exchange");
    }

    @Bean
    public Binding binding(Queue tenantBSlackNotifyQueue, Exchange exchange) {
        return BindingBuilder.bind(tenantBSlackNotifyQueue).to(exchange).with("record.stage.changed").noargs();
    }

    @Bean
    public Binding settingBinding(Queue tenantBSlackNotifySettingQueue, Exchange exchange) {
        return BindingBuilder.bind(tenantBSlackNotifySettingQueue).to(exchange).with("record.changed").noargs();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
