package com.floordecor.inbound.config;

import com.floordecor.inbound.consts.PropertyConstants;
import com.supplychain.foundation.prop.SFTPProp;
import lombok.NoArgsConstructor;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Configuration
@NoArgsConstructor
public class SFTPConfig {
    @Autowired
    private SFTPProp sftpProp;

    @Bean(name = "poInboundSftp")
    public SessionFactory<SftpClient.DirEntry> poInboundSftp() {
        SFTPProp.SFTPCred sftpCred =
                sftpProp.getInboundSftpCred(PropertyConstants.PROP_MMS_PO);
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(false);
        factory.setHost(sftpCred.getHost());
        factory.setPort(sftpCred.getPort());
        factory.setUser(sftpCred.getUsername());
        factory.setPassword(sftpCred.getPassword());
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory, 10);
    }

    @Bean(name = "poOutboundSftp")
    @ConditionalOnProperty("sftp.outbound.host")
    public SessionFactory<SftpClient.DirEntry> poOutboundSftp() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(false);
        factory.setHost(sftpProp.getOutbound().getHost());
        factory.setPort(sftpProp.getOutbound().getPort());
        factory.setUser(sftpProp.getOutbound().getUsername());
        factory.setPassword(sftpProp.getOutbound().getPassword());
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory, 10);
    }

}
