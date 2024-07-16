package com.floordecor.inbound.dto.br;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private List<PackingList> packingList;
}
