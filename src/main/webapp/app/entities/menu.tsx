import React, { useEffect } from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';
import { addTranslationSourcePrefix } from 'app/shared/reducers/locale';
import { useAppDispatch, useAppSelector } from 'app/config/store';

const EntitiesMenu = () => {
  const lastChange = useAppSelector(state => state.locale.lastChange);
  const dispatch = useAppDispatch();
  useEffect(() => {
    dispatch(addTranslationSourcePrefix('services/propservice/'));
  }, [lastChange]);

  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/propservice/complex">
        <Translate contentKey="global.menu.entities.propserviceComplex" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/propservice/property">
        <Translate contentKey="global.menu.entities.propserviceProperty" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/propservice/transaction">
        <Translate contentKey="global.menu.entities.propserviceTransaction" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
