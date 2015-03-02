package com.jingge.dw.kafka;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.jingge.dw.configuration.BaseKafkaConsumerConfiguration;
import com.jingge.dw.handler.MessageHandler;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @DropwizardAppRule can not used in the unit test because the zookeeper and kafka servers must be started before dropwizard.
 *
 * TODO: this unit test does not work right now. But the idea is correct.
 * We only need to make the in memory running zookeeper and kafka servers work.
 */
public class BaseKafkaConsumerAppTest {
    public static final String TOPIC = "test";
    private final String configPath = "config-test.yml";

    private BaseKafkaConsumerConfiguration configuration;
    private BaseKafkaConsumerApp application;
    private Environment environment;
    private Server jettyServer;

    private TestingServer zkTestServer;
    private CuratorFramework cli;
    private int[] port = {9094, 9095};
    private KafkaServerStartable[] kafka = new KafkaServerStartable[2];
    private File zkLogDir;
    private File[] kafkaLogDir = new File[2];

    private String receivedMessage;


    @Before
    public void setUp() throws Exception {
        startZookeeper();
        startKafka(0);
        startKafka(1);
        Producer<String, String> producer = createProducer();
        producer.send(new KeyedMessage<String, String>(TOPIC, "init"));
        producer.close();
        //AdminUtils.createTopic(new ZkClient(zkTestServer.getConnectString()), TOPIC, 1, 1, new Properties());
        startKafkaConsumerApp();
    }

    @After
    public void tearDown() throws Exception {
        stopKafkaConsumerApp();
        stopKafka();
        stopZookeeper();
        for (File file : kafkaLogDir) {
            FileUtils.deleteDirectory(file);
        }
        FileUtils.deleteDirectory(zkLogDir);
    }

    private void startKafkaConsumerApp() {
        startIfRequired();
    }

    /**
     * Attention: this method is coming from DropwizardAppRule
     */
    private void startIfRequired() {
        if (jettyServer != null) {
            return;
        }

        try {
            application = newApplication();

            final Bootstrap<BaseKafkaConsumerConfiguration> bootstrap = new Bootstrap<BaseKafkaConsumerConfiguration>(application) {
                @Override
                public void run(BaseKafkaConsumerConfiguration configuration, Environment environment) throws Exception {
                    environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
                        @Override
                        public void serverStarted(Server server) {
                            jettyServer = server;
                        }
                    });
                    BaseKafkaConsumerAppTest.this.configuration = configuration;
                    BaseKafkaConsumerAppTest.this.environment = environment;
                    super.run(configuration, environment);
                }
            };

            application.initialize(bootstrap);
            final ServerCommand<BaseKafkaConsumerConfiguration> command = new ServerCommand<>(application);

            ImmutableMap.Builder<String, Object> file = ImmutableMap.builder();
            if (!Strings.isNullOrEmpty(configPath)) {
                file.put("file", configPath);
            }
            final Namespace namespace = new Namespace(file.build());

            command.run(bootstrap, namespace);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BaseKafkaConsumerApp newApplication() {
        try {
            return new BaseKafkaConsumerApp() {
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
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stopKafkaConsumerApp() throws Exception {
        jettyServer.stop();
    }


    //@Test
    public void consumeStringTest() throws InterruptedException {
        Producer<String, String> producer = createProducer();
        String data = "Hello Kafka";
        producer.send(new KeyedMessage<String, String>(TOPIC, data));
        producer.close();
        System.out.println("## sending message.");
        Thread.sleep(5);
        System.out.println("## check receivedMessage.");
        assertThat(data).isEqualTo(receivedMessage);
    }

    private Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:" + port[0] + ", localhost:"  + port[1]);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        return new Producer<String, String>(new ProducerConfig(props));
    }

    private void startKafka(int i) {
        // port[i] = InstanceSpec.getRandomPort();
        kafkaLogDir[i] = new File(System.getProperty("java.io.tmpdir"), "kafka/logs/kafka-test-" + port[i]);
        kafkaLogDir[i].deleteOnExit();

        Properties p = new Properties();
        p.setProperty("zookeeper.connect", zkTestServer.getConnectString());
        p.setProperty("broker.id", Integer.toString(port[i]));
        p.setProperty("port", "" + port[i]);
        p.setProperty("log.dirs", kafkaLogDir[i].getAbsolutePath());
        KafkaConfig config = new KafkaConfig(p);
        kafka[i] = new KafkaServerStartable(config);
        kafka[i].startup();
    }

    private void stopKafka() {
        for (int i = 0; i < kafka.length; i++) {
            kafka[i].shutdown();
        }

    }


    private void startZookeeper() throws Exception {
        zkLogDir = new File(System.getProperty("java.io.tmpdir"), "zookeeper/logs/zk-test-" + (int)(Math.random()*1000));
        zkLogDir.deleteOnExit();
        zkTestServer = new TestingServer(2182, zkLogDir);
        cli = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString(),
                new RetryOneTime(20000));
        cli.start();

    }

    private void stopZookeeper() throws IOException {
        cli.close();
        zkTestServer.stop();
    }

    private Path createTempDir(String path) throws IOException {
        //Create a temporary directory
        Path temp = Files.createTempDirectory(Paths.get("path"), "itest");
        //Delete the temporary directory
        temp.toFile().deleteOnExit();
        return temp;
    }


}