package com.floordecor.inbound.init;

import com.floordecor.inbound.mapper.POCustomMapping;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.supplychain.foundation.batch.integration.SftpMessagingGateway;
import com.supplychain.foundation.batch.listener.CommonListener;
import com.supplychain.foundation.service.MessagingService;
import com.supplychain.foundation.service.SFTPService;
import com.supplychain.foundation.utility.IntegrationUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@Configuration
public class CommonBeanInit {
    @Bean
    public CommonListener commonListener() {
        return new CommonListener();}

    @Bean
    POCustomMapping getMapping(){
        return new POCustomMapping();
    }

    @Bean
    public MessagingService messingService(
            @Nullable @Qualifier("sftpOutboundService") SFTPService sftpOutboundService,
            @Nullable JmsTemplate jmsTemplate,
            @Nullable PubSubTemplate pubSubTemplate) {
        return new MessagingService(jmsTemplate, pubSubTemplate, sftpOutboundService) {};
    }

    @ConditionalOnProperty("sftp.outbound.host")
    @Configuration
    public static class SftpOutboundConfig {
        @Bean
        @DependsOn({"poOutboundSftp"})
        @ServiceActivator(inputChannel = "sftpFileOutboundMover")
        public MessageHandler sftpFileMoveOutboundHandler(
                SessionFactory<SftpClient.DirEntry> poOutboundSftp) {
            return IntegrationUtils.sftpFileMoveHandler(poOutboundSftp);
        }

        @Bean
        @DependsOn({"poOutboundSftp"})
        @ServiceActivator(inputChannel = "sftpFileOutboundUpload")
        public MessageHandler sftpFileUploadOutboundHandler(
                SessionFactory<SftpClient.DirEntry> poOutboundSftp) {
            return IntegrationUtils.sftpFileUploadHandler(poOutboundSftp);
        }

        @Bean
        @DependsOn({"poOutboundSftp"})
        @ServiceActivator(inputChannel = "sftpFileOutboundCreate")
        public MessageHandler sftpFileCreateOutboundHandler(
                SessionFactory<SftpClient.DirEntry> poOutboundSftp) {
            return IntegrationUtils.sftpFileCreateHandler(poOutboundSftp);
        }

        @Bean
        @DependsOn({
                "sftpFileMoveOutboundHandler",
                "sftpFileUploadOutboundHandler",
                "sftpFileCreateOutboundHandler"
        })
        public SFTPService sftpOutboundService(
                @Qualifier("sftpOutboundMessagingGateway") SftpOutboundMessagingGateway sftpOutboundMessagingGateway) {
            return new SFTPService(sftpOutboundMessagingGateway);
        }

        @MessagingGateway("sftpOutboundMessagingGateway")
        public interface SftpOutboundMessagingGateway extends SftpMessagingGateway {
            @Gateway(requestChannel = "sftpFileOutboundCreate")
            void createFile(
                    @Payload byte[] content,
                    @Header(FileHeaders.REMOTE_DIRECTORY) String remoteDirectory,
                    @Header(FileHeaders.FILENAME) String fileName);
        }
    }
}
