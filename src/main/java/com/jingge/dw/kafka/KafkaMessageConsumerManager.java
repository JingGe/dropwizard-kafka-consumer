package com.jingge.dw.kafka;

import com.jingge.dw.kafka.KafkaMessageConsumer;
import io.dropwizard.lifecycle.Managed;

/**
 * Created by jing.ge on 10.11.14.
 */
public class KafkaMessageConsumerManager implements Managed {
    private KafkaMessageConsumer kafkaMessageConsumer;

    public KafkaMessageConsumerManager(KafkaMessageConsumer kafkaMessageConsumer) {
        this.kafkaMessageConsumer = kafkaMessageConsumer;
    }

    @Override
    public void start() throws Exception {
        kafkaMessageConsumer.start();
    }

    @Override
    public void stop() throws Exception {
        kafkaMessageConsumer.stop();
    }
}
