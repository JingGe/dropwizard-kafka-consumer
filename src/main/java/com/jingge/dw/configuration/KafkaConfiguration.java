package com.jingge.dw.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class KafkaConfiguration {
    @NotEmpty
    @JsonProperty
    private String zookeeperConnect;

    @NotEmpty
    @JsonProperty
    private String groupId;

    @JsonProperty
    private int zookeeperSessionTimeout = 500;

    @JsonProperty
    private int zookeeperSyncTime = 250;

    @JsonProperty
    private int autoCommitInterval = 1000;

    @NotEmpty
    @JsonProperty
    private String topic;

    public String getZookeeperConnect() {
        return zookeeperConnect;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getZookeeperSessionTimeout() {
        return zookeeperSessionTimeout;
    }

    public int getZookeeperSyncTime() {
        return zookeeperSyncTime;
    }

    public int getAutoCommitInterval() {
        return autoCommitInterval;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return "KafkaConfiguration{" +
                "zookeeperConnect='" + zookeeperConnect + '\'' +
                ", groupId='" + groupId + '\'' +
                ", zookeeperSessionTimeout=" + zookeeperSessionTimeout +
                ", zookeeperSyncTime=" + zookeeperSyncTime +
                ", autoCommitInterval=" + autoCommitInterval +
                ", topic='" + topic + '\'' +
                '}';
    }
}
