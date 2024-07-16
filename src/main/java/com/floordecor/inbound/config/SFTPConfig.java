package com.floordecor.inbound.config;

import com.supplychain.foundation.prop.SFTPProp;
import lombok.AllArgsConstructor;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Configuration
@AllArgsConstructor
public class SFTPConfig {
    @Autowired
    private SFTPProp sftpProp;
    @Bean(name = "mmsAsnInboundSftp")
    public SessionFactory<SftpClient.DirEntry> asnInboundSftp() {
        SFTPProp.SFTPCred sftpCred = sftpProp.getInboundSftpCred("mms_asn");
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(false);
        factory.setHost(sftpCred.getHost());
        factory.setPort(sftpCred.getPort());
        factory.setUser(sftpCred.getUsername());
        factory.setPassword(sftpCred.getPassword());
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory, 10);
    }

    @Bean(name = "brAsnInboundSftp")
    public SessionFactory<SftpClient.DirEntry> brAsnInboundSftp() {
        SFTPProp.SFTPCred sftpCred = sftpProp.getInboundSftpCred("br_asn");
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(false);
        factory.setHost(sftpCred.getHost());
        factory.setPort(sftpCred.getPort());
        factory.setUser(sftpCred.getUsername());
        factory.setPassword(sftpCred.getPassword());
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory, 10);
    }

    @Bean(name = "asnOutboundSftp")
    @ConditionalOnProperty("sftp.outbound.host")
    public SessionFactory<SftpClient.DirEntry> asnOutboundSftp() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(false);
        factory.setHost(sftpProp.getOutbound().getHost());
        factory.setPort(sftpProp.getOutbound().getPort());
        factory.setUser(sftpProp.getOutbound().getUsername());
        factory.setPassword(sftpProp.getOutbound().getPassword());
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory, 10);
    }
}
