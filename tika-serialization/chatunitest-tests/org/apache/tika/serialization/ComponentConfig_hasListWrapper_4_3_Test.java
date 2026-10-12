package org.apache.tika.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentConfig_hasListWrapper_4_3_Test {

    // THIS CHATUNITEST FAILS

    // @Test
    // void testHasListWrapper() {
    // ComponentConfig config = new ComponentConfig.Builder<>("jsonField",
    // String.class).build();
    // assertFalse(config.hasListWrapper());
    // config = new ComponentConfig.Builder<>("jsonField",
    // String.class).loadAsList().build();
    // assertTrue(config.hasListWrapper());
    // }

    // HAND WRITTEN FIX
    @Test
    void HWtestHasListWrapper() {
        ComponentConfig config = new ComponentConfig.Builder<>("jsonField", String.class).build();
        assertFalse(config.hasListWrapper());

        config = new ComponentConfig.Builder<>("jsonField", String.class).wrapWith(list -> list.toString()).build();
        assertTrue(config.hasListWrapper());

    }
}
