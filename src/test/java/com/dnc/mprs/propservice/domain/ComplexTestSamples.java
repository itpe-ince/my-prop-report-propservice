package com.dnc.mprs.propservice.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ComplexTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Complex getComplexSample1() {
        return new Complex()
            .id(1L)
            .complexName("complexName1")
            .state("state1")
            .county("county1")
            .city("city1")
            .town("town1")
            .addressCode("addressCode1");
    }

    public static Complex getComplexSample2() {
        return new Complex()
            .id(2L)
            .complexName("complexName2")
            .state("state2")
            .county("county2")
            .city("city2")
            .town("town2")
            .addressCode("addressCode2");
    }

    public static Complex getComplexRandomSampleGenerator() {
        return new Complex()
            .id(longCount.incrementAndGet())
            .complexName(UUID.randomUUID().toString())
            .state(UUID.randomUUID().toString())
            .county(UUID.randomUUID().toString())
            .city(UUID.randomUUID().toString())
            .town(UUID.randomUUID().toString())
            .addressCode(UUID.randomUUID().toString());
    }
}
