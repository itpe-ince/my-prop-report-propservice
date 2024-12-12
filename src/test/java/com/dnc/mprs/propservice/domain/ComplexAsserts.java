package com.dnc.mprs.propservice.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class ComplexAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertComplexAllPropertiesEquals(Complex expected, Complex actual) {
        assertComplexAutoGeneratedPropertiesEquals(expected, actual);
        assertComplexAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertComplexAllUpdatablePropertiesEquals(Complex expected, Complex actual) {
        assertComplexUpdatableFieldsEquals(expected, actual);
        assertComplexUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertComplexAutoGeneratedPropertiesEquals(Complex expected, Complex actual) {
        assertThat(expected)
            .as("Verify Complex auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertComplexUpdatableFieldsEquals(Complex expected, Complex actual) {
        assertThat(expected)
            .as("Verify Complex relevant properties")
            .satisfies(e -> assertThat(e.getComplexName()).as("check complexName").isEqualTo(actual.getComplexName()))
            .satisfies(e -> assertThat(e.getState()).as("check state").isEqualTo(actual.getState()))
            .satisfies(e -> assertThat(e.getCounty()).as("check county").isEqualTo(actual.getCounty()))
            .satisfies(e -> assertThat(e.getCity()).as("check city").isEqualTo(actual.getCity()))
            .satisfies(e -> assertThat(e.getTown()).as("check town").isEqualTo(actual.getTown()))
            .satisfies(e -> assertThat(e.getAddressCode()).as("check addressCode").isEqualTo(actual.getAddressCode()))
            .satisfies(e -> assertThat(e.getCreatedAt()).as("check createdAt").isEqualTo(actual.getCreatedAt()))
            .satisfies(e -> assertThat(e.getUpdatedAt()).as("check updatedAt").isEqualTo(actual.getUpdatedAt()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertComplexUpdatableRelationshipsEquals(Complex expected, Complex actual) {
        // empty method
    }
}
