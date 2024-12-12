import complex from 'app/entities/propservice/complex/complex.reducer';
import property from 'app/entities/propservice/property/property.reducer';
import transaction from 'app/entities/propservice/transaction/transaction.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  complex,
  property,
  transaction,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
