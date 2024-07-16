package com.floordecor.inbound.processor;


import com.supplychain.mawm.dto.po.MAWMPO;
import com.floordecor.inbound.dto.mms.MMSPO;
import com.floordecor.inbound.mapper.POCustomMapping;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.floordecor.poinbound", "com.supplychain"})
public class MMSPOProcessor {
    private static final CustomLogger log =
            CustomLoggerFactory.getLogger(MMSPOProcessor.class);

    @Bean
    public ItemProcessor<MMSPO, MAWMPO> syncMMSPOProcessor(POCustomMapping mapping){
        return mmspo ->{
            MAWMPO mawmpo= mapping.toMAWMPO(mmspo);
            return mawmpo;
        };
    }

}
