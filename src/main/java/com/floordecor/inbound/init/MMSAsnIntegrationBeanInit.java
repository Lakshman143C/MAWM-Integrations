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
        value = "jobs." + PropertyConstants.PROP_MMS_ASN + ".isEnabled",
        havingValue = "true")
public class MMSAsnIntegrationBeanInit {
    @Bean
    @DependsOn({"mmsAsnInboundSftp"})
    @ServiceActivator(inputChannel = "mmsAsnSftpFileMover")
    public MessageHandler mmsAsnSftpFileMoveHandler(
            SessionFactory<SftpClient.DirEntry> mmsAsnInboundSftp) {
        return IntegrationUtils.sftpFileMoveHandler(mmsAsnInboundSftp);
    }

    @Bean
    @DependsOn({"mmsAsnInboundSftp"})
    @ServiceActivator(inputChannel = "mmsAsnSftpFileUpload")
    public MessageHandler mmsAsnSftpFileUploadHandler(
            SessionFactory<SftpClient.DirEntry> mmsAsnInboundSftp) {
        return IntegrationUtils.sftpFileUploadHandler(mmsAsnInboundSftp);
    }

    @Bean
    @DependsOn({"mmsAsnInboundSftp"})
    @ServiceActivator(inputChannel = "mmsAsnSftpFileCreate")
    public MessageHandler mmsAsnSftpFileCreatedHandler(
            SessionFactory<SftpClient.DirEntry> mmsAsnInboundSftp) {
        return IntegrationUtils.sftpFileCreateHandler(mmsAsnInboundSftp);
    }

    @Bean("mmsAsnSftpService")
    @DependsOn({
            "mmsAsnSftpFileMoveHandler",
            "mmsAsnSftpFileUploadHandler",
            "mmsAsnSftpFileCreatedHandler"
    })
    public SFTPService mmsAsnSftpService(
            @Qualifier("mmsAsnSftpMessagingGateway") MMSAsnSftpMessagingGateway mmsAsnSftpMessagingGateway) {
        return new SFTPService(mmsAsnSftpMessagingGateway);
    }

    @Bean
    @DependsOn({"mmsAsnSftpService"})
    public FileShiftJobListener mmsAsnFileShiftJobListener(SFTPService mmsAsnSftpService) {

        return new FileShiftJobListener(mmsAsnSftpService) {
            @Override
            public boolean isFileMoveToSuccessAllowed(Map<String, String> parameters) {
                return super.isFileMoveToSuccessAllowed(parameters);
            }
        };
    }

    @MessagingGateway("mmsAsnSftpMessagingGateway")
    public interface MMSAsnSftpMessagingGateway extends SftpMessagingGateway {
        @Gateway(requestChannel = "mmsAsnSftpFileMover")
        void moveFile(
                @Payload File file,
                @Header(FileConstants.FROM_PATH) String fromPath,
                @Header(FileConstants.TO_PATH) String toPath);

        @Gateway(requestChannel = "mmsAsnSftpFileUpload")
        void uploadFile(
                @Payload File file,
                @Header(FileHeaders.REMOTE_DIRECTORY) String remoteDirectory,
                @Header(FileHeaders.FILENAME) String fileName);

        @Gateway(requestChannel = "mmsAsnSftpFileCreate")
        void createFile(
                @Payload byte[] content,
                @Header(FileHeaders.REMOTE_DIRECTORY) String remoteDirectory,
                @Header(FileHeaders.FILENAME) String fileName);
    }
}
