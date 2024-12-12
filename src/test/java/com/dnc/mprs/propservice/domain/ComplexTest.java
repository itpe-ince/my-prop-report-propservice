package com.dnc.mprs.propservice.domain;

import static com.dnc.mprs.propservice.domain.ComplexTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.dnc.mprs.propservice.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ComplexTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Complex.class);
        Complex complex1 = getComplexSample1();
        Complex complex2 = new Complex();
        assertThat(complex1).isNotEqualTo(complex2);

        complex2.setId(complex1.getId());
        assertThat(complex1).isEqualTo(complex2);

        complex2 = getComplexSample2();
        assertThat(complex1).isNotEqualTo(complex2);
    }
}
