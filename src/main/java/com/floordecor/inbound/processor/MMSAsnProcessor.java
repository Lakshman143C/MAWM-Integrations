package com.floordecor.inbound.processor;

import com.supplychain.mawm.dto.asn.Asn;
import com.floordecor.inbound.dto.mms.MMSAsn;
import com.floordecor.inbound.mapper.AsnMapper;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import com.supplychain.foundation.utility.JsonUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MMSAsnProcessor {
    private static final CustomLogger log = CustomLoggerFactory.getLogger(MMSAsnProcessor.class);
    @Bean
    public ItemProcessor<MMSAsn, Asn> syncMMSAsnProcessor(AsnMapper asnMapper) {
        return asn -> {
            asn.addReference();
            if(asn.getContainerNumber()==null)
                asn.setContainerNumber(null);
            Asn mawmAsn= asnMapper.toMAWMItem(asn);
            log.debug("Converted MAWM Item , {}", JsonUtils.convert(mawmAsn));
            return mawmAsn;
        };
    }

}
