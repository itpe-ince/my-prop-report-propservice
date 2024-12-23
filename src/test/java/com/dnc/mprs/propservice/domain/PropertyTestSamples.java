package com.dnc.mprs.propservice.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PropertyTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Property getPropertySample1() {
        return new Property()
            .id(1L)
            .address("address1")
            .regionCd("regionCd1")
            .localName("localName1")
            .street("street1")
            .floor(1)
            .type("type1")
            .rooms(1)
            .bathrooms(1)
            .buildYear(1)
            .parkingYn("parkingYn1")
            .description("description1");
    }

    public static Property getPropertySample2() {
        return new Property()
            .id(2L)
            .address("address2")
            .regionCd("regionCd2")
            .localName("localName2")
            .street("street2")
            .floor(2)
            .type("type2")
            .rooms(2)
            .bathrooms(2)
            .buildYear(2)
            .parkingYn("parkingYn2")
            .description("description2");
    }

    public static Property getPropertyRandomSampleGenerator() {
        return new Property()
            .id(longCount.incrementAndGet())
            .address(UUID.randomUUID().toString())
            .regionCd(UUID.randomUUID().toString())
            .localName(UUID.randomUUID().toString())
            .street(UUID.randomUUID().toString())
            .floor(intCount.incrementAndGet())
            .type(UUID.randomUUID().toString())
            .rooms(intCount.incrementAndGet())
            .bathrooms(intCount.incrementAndGet())
            .buildYear(intCount.incrementAndGet())
            .parkingYn(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString());
    }
}
