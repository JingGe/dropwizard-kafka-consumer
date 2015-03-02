package com.jingge.dw.kafka;

import com.jingge.dw.configuration.KafkaConfiguration;
import com.jingge.dw.handler.MessageHandler;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SRP: this class only deals with message consumption with the kafka.
 * Message fetched from kafka will be handed over to the MessageHandler for further processing.
 */
public class KafkaMessageConsumer {

    Logger LOG = LoggerFactory.getLogger(KafkaMessageConsumer.class);

    private ConsumerConnector consumerConnector;
    private KafkaConfiguration kafkaConfiguration;
    private ExecutorService executor;
    private List<MessageHandler> messageHandlers;

    public KafkaMessageConsumer(ConsumerConnector consumerConnector, KafkaConfiguration kafkaConfiguration, List<MessageHandler> messageHandlers) {
        this.consumerConnector = consumerConnector;
        this.kafkaConfiguration = kafkaConfiguration;
        this.messageHandlers = messageHandlers;
        LOG.info("new Kafka ConsumerConnector {} is created with the {}", consumerConnector, kafkaConfiguration);
    }

    public void start() {
        int threadCount = messageHandlers.size();
        String topic = kafkaConfiguration.getTopic();
        Map<String, Integer> topicCount = new HashMap<String, Integer>();
        topicCount.put(topic, new Integer(threadCount));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams =
                consumerConnector.createMessageStreams(topicCount);
        List<KafkaStream<byte[], byte[]>> streams = consumerStreams.
                get(topic);
        executor = Executors.newFixedThreadPool(threadCount);
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ShipmentWorker(stream, messageHandlers.get(threadNumber)));
            threadNumber++;
          LOG.info("New thread has been created for the consumerConnector " + consumerConnector + " to the topic " + topic);
        }
        LOG.info("New consumerConnector " + consumerConnector + " for topic " + topic + " is running.");
    }

    public void stop() {
        if (consumerConnector != null) {
            consumerConnector.shutdown();
            LOG.info("ConsumerConnector {} has been shut down", consumerConnector );
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    class ShipmentWorker implements Runnable {

        private KafkaStream stream;
        private int threadNumber;
        private MessageHandler messageHandler;

        ShipmentWorker(KafkaStream stream, int threadNumber, MessageHandler messageHandler) {
            this.threadNumber = threadNumber;
            this.stream = stream;
            this.messageHandler = messageHandler;
        }

        ShipmentWorker(KafkaStream stream, MessageHandler messageHandler) {
            this.stream = stream;
            this.messageHandler = messageHandler;
        }

        @Override
        public void run() {
            ConsumerIterator<byte[], byte[]> consumerIte = stream.iterator();

            while (consumerIte.hasNext()) {
                byte[] msg = consumerIte.next().message();
                LOG.debug(" consume message: " + msg);
                messageHandler.dispose(msg);
            }
        }
    }
}

