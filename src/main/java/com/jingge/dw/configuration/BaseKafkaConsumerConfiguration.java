package com.jingge.dw.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;


public class BaseKafkaConsumerConfiguration extends Configuration{

    @JsonProperty
    @NotNull
    private MessageHandlerConfiguration messageHandlerConfig = new MessageHandlerConfiguration();

    @JsonProperty
    @NotNull
    private KafkaConfiguration kafkaConfig = new KafkaConfiguration();

    public KafkaConfiguration getKafkaConfig() {
        return kafkaConfig;
    }

    public MessageHandlerConfiguration getMessageHandlerConfig() {
        return messageHandlerConfig;
    }

}
