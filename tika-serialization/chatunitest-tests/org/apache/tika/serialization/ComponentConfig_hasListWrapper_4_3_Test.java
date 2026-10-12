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

@ExtendWith(MockitoExtension.class)
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
