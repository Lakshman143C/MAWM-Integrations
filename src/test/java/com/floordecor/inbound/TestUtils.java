package com.floordecor.inbound;

import com.supplychain.foundation.utility.DateUtils;

import java.util.UUID;

public class TestUtils {
    public static final String DEST_QUEUE_NAME = "item_location";
    public static final String ROOT_DIRECTORY = "inbound";
    public static final String FILE_NAME = "test.csv";
    public static final String JOB_ID = UUID.randomUUID().toString();
    public static final String TRAN_DATE =
            DateUtils.getCurrentUTCTimeStampInString(DateUtils.LONG_DATE_FORMAT);
}
