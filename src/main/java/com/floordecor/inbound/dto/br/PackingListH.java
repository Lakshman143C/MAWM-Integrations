package com.floordecor.inbound.dto.br;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackingListH {
    private String owner;
    private String packingListNo;
    private String supplier;
    private String deliverTo;
    private String status;
    private String createTs;
    private String createUser;
    private String modifyTs;
    private String modifyUser;
    private String status05;
    private String memo1;
    private String memo2;
    private String memo5;
    private String memo7;
    private String memo8;
    private String totQty;
    private String umTotQty;
    private String totPackages;
    private String totGrsWgt;
    private String umTotGrsWgt;
    private String totPackMeas;
    private String totStandardCtn;
    private String totMasterCtn;
    private String totNetWgt;
    private String date3;
    private String date4;
    private String date5;
    private String numbr5;
    private String numbr6;
    private String totPalettes;
    private String totMixedPalettes;
    private String totStdPalettes;
    private String totGrsWgtPlts;
    private List<PackingListD> packingListDetails;
    // Getters and Setters
}
