package com.billing.consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class RabbitConfig implements RabbitListenerConfigurer {

	public static final String EXCHANGE = "ex.billing";
	public static final String QUEUE_IN = "qq.billing.in";
	public static final String QUEUE_DLQ = "qq.billing.dlq";
	public static final String ROUTING_KEY = "billing.routing.key";

	/**
	 * O Boot nao autoconfigura conversor JSON para AMQP: sem este bean o listener receberia
	 * {@code byte[]}. {@link JacksonJsonMessageConverter} e a variante Jackson 3 (nao a
	 * {@code Jackson2JsonMessageConverter} dos tutoriais de Boot 3). Sem {@code __TypeId__} na
	 * mensagem, o type mapper usa o tipo inferido da assinatura do listener, entao o producer
	 * pode publicar JSON puro.
	 */
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}

	@Bean
	public Validator amqpPayloadValidator() {
		return new LocalValidatorFactoryBean();
	}

	/**
	 * Sem este registro o {@code @Valid} do listener e inerte: o Spring AMQP nao valida
	 * payloads por padrao e a mensagem invalida so falharia ao bater nas constraints do banco.
	 */
	@Override
	public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
		registrar.setValidator(amqpPayloadValidator());
	}

	@Bean
	public DirectExchange billingExchange() {
		return new DirectExchange(EXCHANGE, true, false);
	}

	/**
	 * O dead-letter exchange vazio e o default exchange, que roteia pelo nome da fila — evita
	 * criar um DLX novo so para alcancar a {@value #QUEUE_DLQ}.
	 */
	@Bean
	public Queue billingInQueue() {
		return QueueBuilder.durable(QUEUE_IN)
				.deadLetterExchange("")
				.deadLetterRoutingKey(QUEUE_DLQ)
				.build();
	}

	@Bean
	public Queue billingDlq() {
		return QueueBuilder.durable(QUEUE_DLQ).build();
	}

	@Bean
	public Binding billingInBinding() {
		return BindingBuilder.bind(billingInQueue()).to(billingExchange()).with(ROUTING_KEY);
	}

}
