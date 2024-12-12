package com.dnc.mprs.propservice.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Transaction getTransactionSample1() {
        return new Transaction()
            .id(1L)
            .propertyId(1L)
            .transactionType("transactionType1")
            .buyer("buyer1")
            .seller("seller1")
            .agent("agent1");
    }

    public static Transaction getTransactionSample2() {
        return new Transaction()
            .id(2L)
            .propertyId(2L)
            .transactionType("transactionType2")
            .buyer("buyer2")
            .seller("seller2")
            .agent("agent2");
    }

    public static Transaction getTransactionRandomSampleGenerator() {
        return new Transaction()
            .id(longCount.incrementAndGet())
            .propertyId(longCount.incrementAndGet())
            .transactionType(UUID.randomUUID().toString())
            .buyer(UUID.randomUUID().toString())
            .seller(UUID.randomUUID().toString())
            .agent(UUID.randomUUID().toString());
    }
}
