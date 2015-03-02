package com.jingge.dw.kafka;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.jingge.dw.configuration.BaseKafkaConsumerConfiguration;
import com.jingge.dw.handler.MessageHandler;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import kafka.admin.AdminUtils;
import kafka.cluster.Broker;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import net.sourceforge.argparse4j.inf.Namespace;
import org.I0Itec.zkclient.ZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Plz make sure: <br/>
 * 1. a zookeeper is running at localhost:2181.<br/>
 * 2. a kafka server is running at localhost:9092. <br/>
 * 3. a topic named "test" has been created.<br/>
 * before this test class is running.
 * @throws Exception
 */
public class BaseKafkaConsumerAppIT {

    public static final String TOPIC = "test";
    private final String configPath = "config-test.yml";

    private BaseKafkaConsumerConfiguration configuration;
    private BaseKafkaConsumerApp application;
    private Environment environment;
    private Server jettyServer;

    private TestingServer zkTestServer;
    private CuratorFramework cli;
    private int port = 9092;
    private KafkaServerStartable kafka;

    private static String receivedMessage;

    @ClassRule
    public static final DropwizardAppRule<BaseKafkaConsumerConfiguration> RULE =
            new DropwizardAppRule<BaseKafkaConsumerConfiguration>(KafkaConsumerTestApp.class, "config-test.yml");

    @Test
    public void consumeStringTest() throws InterruptedException {
        Producer<String, String> producer = createProducer();
        String data = "Hello Kafka";
        producer.send(new KeyedMessage<String, String>(TOPIC, data));
        Thread.sleep(1);
        assertThat(data).isEqualTo(receivedMessage);
    }

    private Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:" + port);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        return new Producer<String, String>(new ProducerConfig(props));
    }

   public static class KafkaConsumerTestApp extends BaseKafkaConsumerApp {

       public static void main(String[] args) throws Exception {
            new KafkaConsumerTestApp().run(args);
       }

        @Override
        protected MessageHandler createMessageHandler() {
            return new MessageHandler() {
                @Override
                public void start() {

                }

                @Override
                public void stop() {

                }

                @Override
                public void dispose(byte[] message) {
                    receivedMessage = new String(message);
                }
            };
        }
    }

}
