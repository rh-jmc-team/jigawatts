package com.redhat.jigawatts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LibraryLoaderTest {

    @Test
    public void testTrimToNull(){
        Assertions.assertNull(LibraryLoader.trimToNull(null));
        Assertions.assertNull(LibraryLoader.trimToNull(""));
        Assertions.assertNull(LibraryLoader.trimToNull("    "));
        Assertions.assertEquals("hi!", LibraryLoader.trimToNull("  hi!  "));
    }

    @Test
    public void testPropertyToVar() {
        Assertions.assertEquals("PROP", LibraryLoader.propertyToVar("prop"));
        Assertions.assertEquals("_PROP", LibraryLoader.propertyToVar(".prop"));
        Assertions.assertEquals("NICE_PROP", LibraryLoader.propertyToVar("nice.prop"));
    }
}
