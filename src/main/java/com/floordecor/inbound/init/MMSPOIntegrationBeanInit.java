package com.floordecor.inbound.init;

import com.supplychain.foundation.batch.integration.SftpMessagingGateway;
import com.supplychain.foundation.batch.listener.FileShiftJobListener;
import com.supplychain.foundation.consts.FileConstants;
import com.supplychain.foundation.service.SFTPService;
import com.supplychain.foundation.utility.IntegrationUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class MMSPOIntegrationBeanInit {

    @Bean
    @DependsOn({"poInboundSftp"})
    @ServiceActivator(inputChannel = "mmsPOSftpFileMover")
    public MessageHandler mmsPOSftpFileMoveHandler(
            SessionFactory<SftpClient.DirEntry> poInboundSftp) {
        return IntegrationUtils.sftpFileMoveHandler(poInboundSftp);
    }

    @Bean
    @DependsOn({"poInboundSftp"})
    @ServiceActivator(inputChannel = "mmsPOSftpFileUpload")
    public MessageHandler mmsPOSftpFileUploadHandler(
            SessionFactory<SftpClient.DirEntry> poInboundSftp) {
        return IntegrationUtils.sftpFileUploadHandler(poInboundSftp);
    }

    @Bean
    @DependsOn({"poInboundSftp"})
    @ServiceActivator(inputChannel = "mmsPOSftpFileCreate")
    public MessageHandler mmsPOSftpFileCreatedHandler(
            SessionFactory<SftpClient.DirEntry> poInboundSftp) {
        return IntegrationUtils.sftpFileCreateHandler(poInboundSftp);
    }

    @Bean("poSftpService")
    @DependsOn({
            "mmsPOSftpFileMoveHandler",
            "mmsPOSftpFileUploadHandler",
            "mmsPOSftpFileCreatedHandler"
    })
    public SFTPService POSftpService(
            @Qualifier("mmsPOSftpMessagingGateway") mmsPOSftpMessagingGateway mmsPOSftpMessagingGateway) {
        return new SFTPService(mmsPOSftpMessagingGateway);
    }

    @Bean
    @DependsOn({"poSftpService"})
    public FileShiftJobListener poFileShiftJobListener(SFTPService poSftpService) {

        return new FileShiftJobListener(poSftpService) {
            @Override
            public boolean isFileMoveToSuccessAllowed(Map<String, String> parameters) {
                return super.isFileMoveToSuccessAllowed(parameters);
            }
        };
    }


    @MessagingGateway("mmsPOSftpMessagingGateway")
    public interface mmsPOSftpMessagingGateway extends SftpMessagingGateway {
        @Gateway(requestChannel = "mmsPOSftpFileMover")
        void moveFile(
                @Payload File file,
                @Header(FileConstants.FROM_PATH) String fromPath,
                @Header(FileConstants.TO_PATH) String toPath);

        @Gateway(requestChannel = "mmsPOSftpFileUpload")
        void uploadFile(
                @Payload File file,
                @Header(FileHeaders.REMOTE_DIRECTORY) String remoteDirectory,
                @Header(FileHeaders.FILENAME) String fileName);

        @Gateway(requestChannel = "mmsPOSftpFileCreate")
        void createFile(
                @Payload byte[] content,
                @Header(FileHeaders.REMOTE_DIRECTORY) String remoteDirectory,
                @Header(FileHeaders.FILENAME) String fileName);
    }
}
