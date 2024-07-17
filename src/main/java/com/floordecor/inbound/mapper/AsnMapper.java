package com.floordecor.inbound.mapper;

import com.supplychain.mawm.dto.asn.Asn;
import com.supplychain.mawm.dto.asn.AsnLineDto;
import com.floordecor.inbound.dto.mms.ASNLine;
import com.floordecor.inbound.dto.mms.MMSAsn;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AsnMapper {
    @Mappings({
        @Mapping(source = "asnId",target = "asnId"),
        @Mapping(source = "canceled",target = "canceled"),
        @Mapping(source = "asnOriginTypeId",target = "asnOriginTypeId"),
        @Mapping(source = "destinationFacilityId",target = "destinationFacilityId"),
        @Mapping(source = "estimatedDeliveryDate",target = "estimatedDeliveryDate"),
        @Mapping(source = "originFacilityId",target = "originFacilityId",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL),
        @Mapping(source = "vendorId",target = "vendorId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL),
        @Mapping(source = "transferNumber",target = "extendedAttributes.transferNumber",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL) ,
        @Mapping(source = "containerNumber",target = "extendedAttributes.containerNumber",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL),
        @Mapping(source = "asnLines", target = "asnLines")
    })
    Asn toMAWMAsn(MMSAsn source);
    @Mapping(source = "asn.asnId", target = "asnId")
    AsnLineDto toASNLineDto(ASNLine asnLine);
    List<AsnLineDto> mapAsnLines(Set<ASNLine> asnLines); // Add this method to handle ASNLine mapping

}
