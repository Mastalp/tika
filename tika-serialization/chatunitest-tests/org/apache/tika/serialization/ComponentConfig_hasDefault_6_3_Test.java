package org.apache.tika.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentConfig_hasDefault_6_3_Test {

    // THIS CHATUNITEST FAILS

    // @Test
    // void hasDefault() {
    // ComponentConfig config = new ComponentConfig.Builder<>("jsonField",
    // String.class).build();
    // assertTrue(config.hasDefault());
    // config = new ComponentConfig.Builder<>("jsonField", Integer.class).build();
    // assertFalse(config.hasDefault());
    // }

    // HAND WRITTEN FIX

    @Test
    void HWhasDefault() {
        ComponentConfig config = new ComponentConfig.Builder<>("jsonField", String.class).build();

        assertFalse(config.hasDefault());

        config = new ComponentConfig.Builder<>("jsonField", String.class)
                .defaultProvider(() -> "default value").build();

        assertTrue(config.hasDefault());
    }
}
