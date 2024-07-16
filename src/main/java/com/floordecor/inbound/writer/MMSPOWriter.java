package com.floordecor.inbound.writer;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.supplychain.mawm.prop.MAWMProp;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.supplychain.mawm.dto.po.MAWMData;
import com.supplychain.mawm.dto.po.MAWMPO;
import com.supplychain.foundation.consts.CommonConstants;
import com.supplychain.foundation.consts.JobConstants;
import com.supplychain.foundation.prop.JobProp;
import com.supplychain.foundation.service.MessagingService;
import com.supplychain.foundation.utility.DateUtils;
import com.supplychain.foundation.utility.QueueHeaderUtils;
import com.supplychain.foundation.utility.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;

@Configuration
@AllArgsConstructor
public class MMSPOWriter {

    @Autowired(required = false) private MessagingService messagingService;

    @Bean
    @StepScope
    public ItemWriter<MAWMPO> syncMMSPOWriter(@Value("#{jobParameters['" + JobConstants.JOB_ID + "']}") String jobId,
                                              @Value("#{jobParameters['" + JobConstants.INTERFACE_TYPE + "']}") String interfaceType,
                                              @Value("#{jobParameters['" + JobConstants.INPUT_FILE_NAME + "']}") String fileName,
                                              JobProp jobProp,
                                              MAWMProp mawmProp){
        return pos -> {
            String parentProp = InterfaceTypes.valueOf(interfaceType).getType();
            ObjectMapper objectMapper = new ObjectMapper();

            MAWMData mawmPOData=new MAWMData();
            mawmPOData.setMawmpoList(new ArrayList<>(pos.getItems()));


            String jsonPayload = objectMapper.writeValueAsString(mawmPOData);//pos.getItems().get(0)
            messagingService.send(parentProp,jobProp.get(parentProp),
                    jsonPayload,
                    QueueHeaderUtils.generateQueueHeaders(
                            mawmProp.getOrgId(),
                            null,
                            StringUtils.getUniqueId(),
                            new HashMap<>(){
                                {
                                    put(JobConstants.INPUT_FILE_NAME, fileName);
                                    put(CommonConstants.MSG_TYPE, DateUtils.getCurrentTimeInString());
                                    put(JobConstants.JOB_ID, jobId);
                                }
                            }));

        };

    }
}
