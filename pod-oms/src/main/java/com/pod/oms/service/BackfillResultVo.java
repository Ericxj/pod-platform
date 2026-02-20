package com.pod.oms.service;

/**
 * P1.6++ 预回填结果值对象，带可选 reasonMsg（如 NOT_MATCHED 时未匹配 sku/asin）。
 */
public class BackfillResultVo {
    private final BackfillResult result;
    private final String reasonMsg;

    public BackfillResultVo(BackfillResult result, String reasonMsg) {
        this.result = result;
        this.reasonMsg = reasonMsg;
    }

    public static BackfillResultVo of(BackfillResult result) {
        return new BackfillResultVo(result, null);
    }

    public static BackfillResultVo notMatched(String reasonMsg) {
        return new BackfillResultVo(BackfillResult.NOT_MATCHED, reasonMsg);
    }

    public BackfillResult getResult() { return result; }
    public String getReasonMsg() { return reasonMsg; }
}
