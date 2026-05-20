package com.jointpay.joinpay;

import com.jointpay.api.profitsharing.ProfitSharingMode;
import com.jointpay.api.profitsharing.ProfitSharingParticipant;
import com.jointpay.api.profitsharing.ProfitSharingScheme;
import com.jointpay.common.json.Jsons;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 将统一分账方案序列化为汇聚可透传的 JSON（预下单 extras / OpenAPI 复用）。 */
final class JoinPaySharingSchemeEncoder {

    private JoinPaySharingSchemeEncoder() {
    }

    static String toJson(ProfitSharingScheme scheme) {
        Map<String, Object> root = new HashMap<>();
        root.put("schemeId", scheme.getSchemeId());
        root.put("acctInfos", toAcctInfos(scheme.getParticipants()));
        return Jsons.toJson(root);
    }

    private static List<Map<String, String>> toAcctInfos(List<ProfitSharingParticipant> participants) {
        List<Map<String, String>> list = new ArrayList<>();
        for (ProfitSharingParticipant p : participants) {
            Map<String, String> item = new HashMap<>();
            String accountNo = firstNonBlank(p.getAccountNo(), p.getMerchantId(), p.getParticipantId());
            item.put("accountNo", accountNo);
            if (p.getMode() != ProfitSharingMode.RATIO) {
                item.put("amount", toAmountYuan(p.getAmountCent()));
            }
            item.put("description", p.getRole() == null ? "分账" : p.getRole());
            list.add(item);
        }
        return list;
    }

    private static String toAmountYuan(long amountCent) {
        return BigDecimal.valueOf(amountCent, 2).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
