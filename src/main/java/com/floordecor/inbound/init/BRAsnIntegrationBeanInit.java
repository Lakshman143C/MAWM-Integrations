package com.floordecor.inbound.init;

import com.floordecor.inbound.consts.PropertyConstants;
import com.supplychain.foundation.batch.integration.SftpMessagingGateway;
import com.supplychain.foundation.batch.listener.FileShiftJobListener;
import com.supplychain.foundation.consts.FileConstants;
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
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.io.File;
import java.util.Map;

@Configuration
@ConditionalOnProperty(
        value = "jobs." + PropertyConstants.PROP_BR_ASN + ".isEnabled",
        havingValue = "true")
public class BRAsnIntegrationBeanInit {
    @Bean
    @DependsOn({"brAsnInboundSftp"})
    @ServiceActivator(inputChannel = "brAsnSftpFileMover")
    public MessageHandler brAsnSftpFileMoveHandler(
            SessionFactory<SftpClient.DirEntry> brAsnInboundSftp) {
        return IntegrationUtils.sftpFileMoveHandler(brAsnInboundSftp);
    }

    @Bean
    @DependsOn({"brAsnInboundSftp"})
    @ServiceActivator(inputChannel = "brAsnSftpFileUpload")
    public MessageHandler brAsnSftpFileUploadHandler(
            SessionFactory<SftpClient.DirEntry> brAsnInboundSftp) {
        return IntegrationUtils.sftpFileUploadHandler(brAsnInboundSftp);
    }

    @Bean
    @DependsOn({"brAsnInboundSftp"})
    @ServiceActivator(inputChannel = "brAsnSftpFileCreate")
    public MessageHandler brAsnSftpFileCreatedHandler(
            SessionFactory<SftpClient.DirEntry> brAsnInboundSftp) {
        return IntegrationUtils.sftpFileCreateHandler(brAsnInboundSftp);
    }

    @Bean("brAsnSftpService")
    @DependsOn({
            "brAsnSftpFileMoveHandler",
            "brAsnSftpFileUploadHandler",
            "brAsnSftpFileCreatedHandler"
    })
    public SFTPService brAsnSftpService(
            @Qualifier("brAsnSftpMessagingGateway") BRAsnSftpMessagingGateway brAsnSftpMessagingGateway) {
        return new SFTPService(brAsnSftpMessagingGateway);
    }

    @Bean
    @DependsOn({"brAsnSftpService"})
    public FileShiftJobListener brAsnFileShiftJobListener(SFTPService brAsnSftpService) {

        return new FileShiftJobListener(brAsnSftpService) {
            @Override
            public boolean isFileMoveToSuccessAllowed(Map<String, String> parameters) {
                return super.isFileMoveToSuccessAllowed(parameters);
            }
        };
    }

    @MessagingGateway("brAsnSftpMessagingGateway")
    public interface BRAsnSftpMessagingGateway extends SftpMessagingGateway {
        @Gateway(requestChannel = "brAsnSftpFileMover")
        void moveFile(
                @Payload File file,
                @Header(FileConstants.FROM_PATH) String fromPath,
                @Header(FileConstants.TO_PATH) String toPath);

        @Gateway(requestChannel = "brAsnSftpFileUpload")
        void uploadFile(
                @Payload File file,
                @Header(FileHeaders.REMOTE_DIRECTORY) String remoteDirectory,
                @Header(FileHeaders.FILENAME) String fileName);

        @Gateway(requestChannel = "brAsnSftpFileCreate")
        void createFile(
                @Payload byte[] content,
                @Header(FileHeaders.REMOTE_DIRECTORY) String remoteDirectory,
                @Header(FileHeaders.FILENAME) String fileName);
    }
}
