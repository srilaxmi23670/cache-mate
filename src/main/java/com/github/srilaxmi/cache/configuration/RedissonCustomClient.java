package com.github.srilaxmi.cache.configuration;

import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.StringCodec;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.ReadMode;

@Component
@Slf4j
public class RedissonCustomClient {

    @Value("${redis.topology}")
    private String topology;

    @Value("${redis.ssl}")
    private boolean ssl;

    @Value("${redis.uri}")
    private String uri;

    @Value("${redis.password}")
    private String password;

    public RedissonClient getRedissonClient() {

        Config config = new Config();
        StringCodec codec = new StringCodec();
        config.setCodec(codec);

        if (topology.equals("cluster")) {
            setupRedisClusterConnection(config);
        } else {
            setupRedisStandaloneConnection(config);
        }

        return Redisson.create(config);
    }

    private void setupRedisStandaloneConnection(Config config) {

        log.info("REDISSON :: connecting to single server");

        config.useSingleServer()
                .setAddress(uri)
                .setTimeout(60000)
                .setSslEnableEndpointIdentification(ssl)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(5)
                .setDatabase(0);
        if (!StringUtils.isBlank(password)) {
            log.info("REDISSON :: Using password");
            config.useSingleServer().setPassword(password);
        }
    }

    private void setupRedisClusterConnection(Config config) {

        log.info("REDISSON :: connecting to cluster");

        ClusterServersConfig clusterServersConfig = config.useClusterServers()
                .addNodeAddress(uri)
                .setTimeout(60000)
                .setTcpNoDelay(true)
                .setReadMode(ReadMode.MASTER_SLAVE)
                .setKeepAlive(true)
                .setSslEnableEndpointIdentification(ssl)
                .setRetryAttempts(10);
        if (!StringUtils.isBlank(password)) {
            clusterServersConfig.setPassword(password);
        }
    }

}

