package com.floordecor.inbound.dto.br;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PackingList {
    private Messages messages;
    private List<PackingListH> packingListHeaders;
}
