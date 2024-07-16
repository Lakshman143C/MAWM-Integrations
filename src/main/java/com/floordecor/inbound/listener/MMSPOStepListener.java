package com.floordecor.inbound.listener;

import com.floordecor.inbound.dto.mms.MMSPO;
import com.floordecor.inbound.dto.prop.PoProps;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import com.supplychain.foundation.prop.JobProp;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.HashMap;


import static com.floordecor.inbound.consts.PropertyConstants.PROP_ENTITY_CREATE;

@Component
@AllArgsConstructor
public class MMSPOStepListener implements ItemWriteListener<MMSPO> {

    private static final CustomLogger log = CustomLoggerFactory.getLogger(MMSPOStepListener.class);

    @Autowired private PoProps props;


    @Override
    public void afterWrite(Chunk<? extends MMSPO> pos) {


    }

    private JobProp.JobDetail buildJobDetail() {
        return JobProp.JobDetail.builder()
                .destination(
                        JobProp.Destination.builder()
                                .destType(JobProp.DestType.JMS)
                                .queues(
                                        new HashMap<>() {
                                            {
                                                put(
                                                        PROP_ENTITY_CREATE,
                                                        JobProp.QueueInfo.builder()
                                                                .name(props.getEntityCreateEvent().get("item").getQueue())
                                                                .build());
                                            }
                                        })
                                .build())
                .build();
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends MMSPO> mmspos) {
    }

}
