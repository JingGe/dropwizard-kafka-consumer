package com.jingge.dw.kafka;

import com.jingge.dw.configuration.BaseKafkaConsumerConfiguration;
import com.jingge.dw.configuration.KafkaConfiguration;
import com.jingge.dw.handler.LogMessageHandler;
import com.jingge.dw.handler.MessageHandler;
import com.jingge.dw.resource.NOOPResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Base DW app, which can/should be extended to override the #createMessageHandler() method for processing the message with your wished logic.
 */
public class BaseKafkaConsumerApp extends Application<BaseKafkaConsumerConfiguration>{

    Logger LOG = LoggerFactory.getLogger(BaseKafkaConsumerApp.class);

    public static void main(String[] args) throws Exception {
        new BaseKafkaConsumerApp().run(args);
    }

    @Override
    public void initialize(Bootstrap<BaseKafkaConsumerConfiguration> bootstrap) {

    }

    protected MessageHandler createMessageHandler() {
        return new LogMessageHandler();
    }


    @Override
    public void run(BaseKafkaConsumerConfiguration configuration, Environment environment) throws Exception {
        List<MessageHandler> messageHandlers= new ArrayList<MessageHandler>(configuration.getMessageHandlerConfig().getNumber());
        for (int i = 0; i < configuration.getMessageHandlerConfig().getNumber(); i++) {
            messageHandlers.add(createMessageHandler());
        }
        MessageHandlerManager messageHandlerManager = new MessageHandlerManager(messageHandlers);
        environment.lifecycle().manage(messageHandlerManager);
        LOG.info("new MessageHandlerManager with {} MessageHandlers has been created " +
                "and managed by the dropwizard environment.", messageHandlers.size());

        KafkaMessageConsumer consumer = createKafkaConsumer(configuration.getKafkaConfig(), messageHandlers);
        KafkaMessageConsumerManager kafkaMessageConsumerManager =
                new KafkaMessageConsumerManager(consumer);
        environment.lifecycle().manage(kafkaMessageConsumerManager);
        LOG.info("new KafkaMessageConsumerManager with a KafkaMessageConsumer has been created " +
                "and managed by the dropwizard environment.");
        registerResources(environment);
    }

    protected void registerResources(Environment environment) {
        environment.jersey().register(new NOOPResource());
        LOG.info("Resource {} registered", NOOPResource.class.getName());
    }

    private KafkaMessageConsumer createKafkaConsumer(KafkaConfiguration kafkaConfiguration, List<MessageHandler> messageHandlers) {
        Properties props = new Properties();
        props.put("zookeeper.connect", kafkaConfiguration.getZookeeperConnect());
        props.put("group.id", kafkaConfiguration.getGroupId());
        props.put("zookeeper.session.timeout.ms", Integer.toString(kafkaConfiguration.getZookeeperSessionTimeout()));
        props.put("zookeeper.sync.time.ms", Integer.toString(kafkaConfiguration.getZookeeperSyncTime()));
        props.put("auto.commit.interval.ms", Integer.toString(kafkaConfiguration.getAutoCommitInterval()));
        ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(
                new ConsumerConfig(props));
        LOG.info("new Kafka ConsumerConnector {} is created with the {}", consumerConnector, kafkaConfiguration);
        return new KafkaMessageConsumer(consumerConnector, kafkaConfiguration, messageHandlers);
    }
}
