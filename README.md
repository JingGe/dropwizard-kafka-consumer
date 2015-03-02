# Common Kafka Consumer as a Microservice

More and more people are talking about microservices architecture and in many projects kafka have been used as the dumb pipeline.
This project is trying to build a standard kafka consumer, which can be used as the base for further extension. 
Dropwizard framework is used in this project to make it run as a microservice.

## Core Components

There are two key components built in this project based on the SRP design principle. 

### KafkaMessageConsumer - handle the kafka topic consumption

The KafkaMessageConsumer has two methods start() and stop(). 
After calling the start() method the KafkaMessageConsumer will be ready to consume any incoming message for the given topic.
Calling the stop() method will release resources. 
The lifecycle of KafkaMessageConsumer will be controlled by the class KafkaMessageConsumerManager, which implements the io.dropwizard.lifecycle.Managed interface. 
This means the start() and stop() method of a KafkaMessageConsumer instance will be called as the application is started and stopped.
The io.dropwizard.lifecycle.Managed interface provided by dropwizard is a really nice feature. Please take a look at the KafkaMessageConsumerManager. 
The code will tell you what is going on. No need to describe it in any plain text.


### MessageHandler - handle the message got from the kafka

A MessageHandler interface is defined in this project. This interface will concentrate on how to handle the message(SRP principle). 
Just like the KafkaMessageConsumer class, this interface also has the start() and stop() method for the lifecycle management, which will be handled by the MessageHandlerManager(another example of using SRP principle).

The class LogMessageHandler is just a example to show you how to use this project as a foundation and how to implement the MessageHandler interface.

## Configuration

Configurations are split into two classes.

### KafkaConfiguration

Following configuration regarding to Kafka can be configured in the yml file, for example:

  kafkaConfig:
    
    topic: test
    
    zookeeperConnect: localhost:2181
    
    groupId: testGroup
   
    zookeeperSessionTimeout: 5000
    
    zookeeperSyncTime: 250
    
    autoCommitInterval: 1000


### MessageHandlerConfiguration

The kafka API allow consuming many partitions in parallel. The KafkaMessageConsumer is also built to support this feature. 
Since the relationship between the KafkaMessageConsume and MessageHandler is one-to-one, you can configure the number of consumers like this:

  messageHandlerConfig:
   
    number: 1

Increasing the number will add more kafka consumer. Please make sure you have defined enough partitions of given topic. 

## Dumb Resource

A dumb resource with no operation is created to make the microservice runnable.

## Test

Two kinds of tests are built for this project. 

### Unit Test

In this test, zookeeper, kafka, and the our App are running in memory. Strictly speaking, this test is a integration test. 
Since there are some problems with running the kafka in memory, this test does not work right now.

### Integration Test

Before running this integration test, please make sure that:

 1. a zookeeper is running at localhost:2181.<br/>
 2. a kafka server is running at localhost:9092. <br/>
 3. a topic named "test" has been created.<br/>
 