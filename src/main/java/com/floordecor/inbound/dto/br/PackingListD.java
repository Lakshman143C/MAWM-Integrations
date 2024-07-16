package com.floordecor.inbound.dto.br;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackingListD {
    private String owner;
    private String packingListNo;
    private String rowNo;
    private String orderNo;
    private String styleNo;
    private String shipPack;
    private String qty;
    private String umQty;
    private String noPackages;
    private String noInners;
    private String qtyPerInner;
    private String grsWgtPack;
    private String umGrsWgtPack;
    private String modifyTs;
    private String modifyUser;
    private String orderOwner;
    private String proformaPo;
    private String orderRowNo;
    private String status01;
    private String status04;
    private String memo1;
    private String memo2;
    private String memo3;
    private String memo4;
    private String umQtyPerPack;
    private String umUnitPerPack;
    private String umUnitPerPpk;
    private String umInners;
    private String umQtyPerInner;
    private String umQtyPerPak;
    private String itemNo;
    private String numbr1;
    private String numbr6;
    private String date1;
    private String allocRatio;
    private String itemWgt;
    private String ctnsPerPallet;
    private String palletsType;
    private String itemWgtPerPack;
    private String grsWgtPallet;
    private String noPallets;
}