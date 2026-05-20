package com.jointpay.api.profitsharing;

import java.util.List;
import java.util.Map;

/**
 * 分账方案：参与方列表 + 可选方案标识。
 */
public final class ProfitSharingScheme {

    private final String schemeId;
    private final List<ProfitSharingParticipant> participants;
    private final Map<String, String> extras;

    public ProfitSharingScheme(
            String schemeId,
            List<ProfitSharingParticipant> participants,
            Map<String, String> extras) {
        this.schemeId = schemeId;
        this.participants = participants == null ? List.of() : List.copyOf(participants);
        this.extras = extras == null ? Map.of() : Map.copyOf(extras);
    }

    public String getSchemeId() {
        return schemeId;
    }

    public List<ProfitSharingParticipant> getParticipants() {
        return participants;
    }

    public Map<String, String> getExtras() {
        return extras;
    }
}
