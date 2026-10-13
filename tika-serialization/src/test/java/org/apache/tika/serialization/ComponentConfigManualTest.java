/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ComponentConfigManualTest {

    @Test
    void testGetJsonField() {
        ComponentConfig<String> config =
                new ComponentConfig.Builder<>("parsers", String.class)
                        .build();

        assertEquals("parsers", config.getJsonField());
    }
    
    @Test
    void testIsLoadAsList() {
        ComponentConfig<String> config =
                new ComponentConfig.Builder<>("parsers", String.class)
                        .build();

        assertFalse(config.isLoadAsList());

        config = new ComponentConfig.Builder<>("parsers", String.class)
            .loadAsList()
            .build();

        assertTrue(config.isLoadAsList());
    }
    
    @Test
    void testGetComponentClass() {
        ComponentConfig<String> config =
                new ComponentConfig.Builder<>("parsers", String.class)
                        .build();

        assertEquals(String.class, config.getComponentClass());
    }
    
    @Test
    void testGetDefault() {
        ComponentConfig<String> config =
                new ComponentConfig.Builder<>("parsers", String.class)
                        .defaultProvider(() -> "valeur par defaut")
                        .build();

        assertEquals("valeur par defaut", config.getDefault());
    }
    
    @Test
    void testGetCustomLoader() {
        org.apache.tika.config.loader.ComponentLoader<String> loader =
                (json, context) -> "chargeur personnalise";

        ComponentConfig<String> config =
                new ComponentConfig.Builder<>("parsers", String.class)
                        .customLoader(loader)
                        .build();

        assertTrue(config.hasCustomLoader());
        assertSame(loader, config.getCustomLoader());
    }

    @Test
    void testWrapList() {
        ComponentConfig<String> config =
                new ComponentConfig.Builder<>("parsers", String.class)
                        .wrapWith(list -> String.join(",", 
                            list.stream().map(Object::toString).toList()))
                        .build();

        assertEquals("A,B", config.wrapList(java.util.List.of("A", "B")));

        ComponentConfig<String> configWithoutWrapper =
                new ComponentConfig.Builder<>("parsers", String.class)
                        .build();

        assertThrows(IllegalStateException.class,
                () -> configWithoutWrapper.wrapList(java.util.List.of("A", "B")));
    }




}
