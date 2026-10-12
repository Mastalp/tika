package org.apache.tika.serialization;

import org.apache.tika.metadata.TikaCoreProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PrettyMetadataKeyComparator_compare_0_0_Test {

    @Test
    void compare_nullStrings_returns1() {
        String s1 = null;
        String s2 = null;
        int result = new PrettyMetadataKeyComparator().compare(s1, s2);
        assertEquals(1, result);
    }

    // @Test
    // void compare_nullString_returnsMinus1() {
    // String s1 = null;
    // String s2 = "some string";
    // int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    // assertEquals(-1, result);
    // }

    // @Test
    // void compare_someString_returnsMinus2() {
    // String s1 = "some string";
    // String s2 = null;
    // int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    // assertEquals(-2, result);
    // }

    @Test
    void compare_tikaContent_returns0() {
        String s1 = TikaCoreProperties.TIKA_CONTENT.getName();
        String s2 = TikaCoreProperties.TIKA_CONTENT.getName();
        int result = new PrettyMetadataKeyComparator().compare(s1, s2);
        assertEquals(0, result);
    }

    // @Test
    // void compare_tikaContent_returns2() {
    // String s1 = "some string";
    // String s2 = TikaCoreProperties.TIKA_CONTENT.getName();
    // int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    // assertEquals(2, result);
    // }

    // @Test
    // void compare_tikaContent_returnsMinus2() {
    // String s1 = TikaCoreProperties.TIKA_CONTENT.getName();
    // String s2 = "some string";
    // int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    // assertEquals(-2, result);
    // }

    //
    // @Test
    // void compare_lowercase_returnsMinus1() {
    // String s1 = "some string";
    // String s2 = "SOME STRING";
    // int result = new PrettyMetadataKeyComparator().compare(s1, s2);
    // assertEquals(-1, result);
    // }

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
