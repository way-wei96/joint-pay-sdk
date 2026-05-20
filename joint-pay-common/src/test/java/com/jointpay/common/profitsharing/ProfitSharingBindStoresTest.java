package com.jointpay.common.profitsharing;

import com.jointpay.api.profitsharing.ProfitSharingParticipant;
import com.jointpay.api.profitsharing.ProfitSharingScheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProfitSharingBindStoresTest {

    @AfterEach
    void reset() {
        ProfitSharingBindStores.use(new InMemoryProfitSharingBindStore());
    }

    @Test
    void takeRemovesBinding() {
        ProfitSharingScheme scheme = new ProfitSharingScheme(
                "S1",
                List.of(ProfitSharingParticipant.builder()
                        .participantId("P1")
                        .amountCent(10L)
                        .build()),
                Map.of());

        ProfitSharingBindStores.put("ORDER1", scheme);
        assertEquals(scheme, ProfitSharingBindStores.take("ORDER1"));
        assertNull(ProfitSharingBindStores.take("ORDER1"));
    }
}
