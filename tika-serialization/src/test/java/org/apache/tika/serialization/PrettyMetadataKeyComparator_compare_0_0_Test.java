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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.tika.metadata.TikaCoreProperties;

@ExtendWith(MockitoExtension.class)
class PrettyMetadataKeyComparator_compare_0_0_Test {

    @Test
    void compare_nullStrings_returns1() {
        String s1 = null;
        String s2 = null;
        int result = new PrettyMetadataKeyComparator().compare(s1, s2);
        assertEquals(1, result);
    }

    //@Test
    //void compare_nullString_returnsMinus1() {
    //    String s1 = null;
    //    String s2 = "some string";
    //    int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    //    assertEquals(-1, result);
    //}

    //@Test
    //void compare_someString_returnsMinus2() {
    //    String s1 = "some string";
    //    String s2 = null;
    //    int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    //    assertEquals(-2, result);
    //}

    @Test
    void compare_tikaContent_returns0() {
        String s1 = TikaCoreProperties.TIKA_CONTENT.getName();
        String s2 = TikaCoreProperties.TIKA_CONTENT.getName();
        int result = new PrettyMetadataKeyComparator().compare(s1, s2);
        assertEquals(0, result);
    }

    //@Test
    //void compare_tikaContent_returns2() {
    //    String s1 = "some string";
    //    String s2 = TikaCoreProperties.TIKA_CONTENT.getName();
    //    int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    //    assertEquals(2, result);
    //}

    //@Test
    //void compare_tikaContent_returnsMinus2() {
    //    String s1 = TikaCoreProperties.TIKA_CONTENT.getName();
    //    String s2 = "some string";
    //    int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    //    assertEquals(-2, result);
    //}

    //
    //@Test
    //void compare_lowercase_returnsMinus1() {
    //    String s1 = "some string";
    //    String s2 = "SOME STRING";
    //    int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    //    assertEquals(-1, result);
    //}

    // HAND WRITTEN TESTS

    @Test
    void HWcontentKeyHasPriorityOverOrdinaryKeys() {
        String content = TikaCoreProperties.TIKA_CONTENT.getName();

        int result = new PrettyMetadataKeyComparator().compare(content, "ordinary-key");

        assertTrue(result > 0);
    }

    // hand written version of compare_lowercase_returnsMinus1()
    @Test
    void HWcompare_lowercase_returnsMinus1() {
        String s1 = "some string";
        String s2 = "SOME STRING";
        int result = new PrettyMetadataKeyComparator().compare(s1, s2);
        assertTrue(result > 0);
    }

    // hand written version of compare_nullString_returnsMinus1()
    @Test
    void HWcompare_nullString_returnsMinus1() {
        String s1 = null;
        String s2 = "some string";

        int result = new PrettyMetadataKeyComparator().compare(s1, s2);

        assertTrue(result > 0);
    }

    // hand written version of compare_someString_returnsMinus2()
    @Test
    void HWcompare_someString_returnsMinus2() {
        String s1 = "some string";
        String s2 = null;

        int result = new PrettyMetadataKeyComparator().compare(s1, s2);

        assertTrue(result < 0);
    }

    // hand written version of compare_tikaContent_returns2()
    @Test
    void HWcompare_tikaContent_returns2() {
        String s1 = "some string";
        String s2 = TikaCoreProperties.TIKA_CONTENT.getName();

        int result = new PrettyMetadataKeyComparator().compare(s1, s2);

        assertEquals(-2, result);
        
    }
    
    // hand written version of compare_tikaContent_returnsMinus2()
    @Test
    void HWcompare_tikaContent_returnsMinus2() {
        String s1 = TikaCoreProperties.TIKA_CONTENT.getName();
        String s2 = "some string";

        int result = new PrettyMetadataKeyComparator().compare(s1, s2);

        assertEquals(2, result);
    }
}
