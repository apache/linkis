package org.apache.linkis.metadatamanager.service;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

public class KafkaConnection implements Closeable {
    private AdminClient adminClient;

    public KafkaConnection(String uris) throws Exception{
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uris);
        adminClient = KafkaAdminClient.create(props);
    }

    public KafkaConnection(String uris, String principle, String keytabFilePath) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, uris);
        adminClient = KafkaAdminClient.create(props);
    }

    public AdminClient getClient(){
        return adminClient;
    }

    @Override
    public void close() throws IOException {
        adminClient.close();
    }
}
