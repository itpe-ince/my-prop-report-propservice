package com.dnc.mprs.propservice.repository;

import com.dnc.mprs.propservice.domain.Complex;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Complex entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ComplexRepository extends JpaRepository<Complex, Long> {}
