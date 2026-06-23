package com.featureflag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaVersionTest {

    @Test
    void runsOnJava21() {
        assertEquals(21, Runtime.version().feature(),
                "Tests must run on Java 21; set JAVA_HOME to JDK 21");
    }
}
