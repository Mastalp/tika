package org.apache.tika.serialization;

import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.tika.config.loader.ComponentLoader;

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
