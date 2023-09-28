package com.redhat.jigawatts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LibraryLoaderTest {

    @Test
    public void testTrimToNull() {
        Assertions.assertNull(LibraryLoader.trimToNull(null, true));
        Assertions.assertNull(LibraryLoader.trimToNull("", true));
        Assertions.assertNull(LibraryLoader.trimToNull("    ", true));
        Assertions.assertEquals("hi!", LibraryLoader.trimToNull("  hi!  ", true));
    }

    public void testTrimToWithouNUll() {
        Assertions.assertNull(LibraryLoader.trimToNull(null, false));
        Assertions.assertEquals("", LibraryLoader.trimToNull("", false));
        Assertions.assertEquals("", LibraryLoader.trimToNull("    ", false));
        Assertions.assertEquals("hi!", LibraryLoader.trimToNull("  hi!  ", false));
    }

    @Test
    public void testPropertyToVar() {
        Assertions.assertEquals("PROP", LibraryLoader.propertyToVar("prop"));
        Assertions.assertEquals("_PROP", LibraryLoader.propertyToVar(".prop"));
        Assertions.assertEquals("NICE_PROP", LibraryLoader.propertyToVar("nice.prop"));
    }
}
