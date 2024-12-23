import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getComplexes } from 'app/entities/propservice/complex/complex.reducer';
import { createEntity, getEntity, reset, updateEntity } from './property.reducer';

export const PropertyUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const complexes = useAppSelector(state => state.propservice.complex.entities);
  const propertyEntity = useAppSelector(state => state.propservice.property.entity);
  const loading = useAppSelector(state => state.propservice.property.loading);
  const updating = useAppSelector(state => state.propservice.property.updating);
  const updateSuccess = useAppSelector(state => state.propservice.property.updateSuccess);

  const handleClose = () => {
    navigate(`/propservice/property${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getComplexes({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    if (values.floor !== undefined && typeof values.floor !== 'number') {
      values.floor = Number(values.floor);
    }
    if (values.area !== undefined && typeof values.area !== 'number') {
      values.area = Number(values.area);
    }
    if (values.rooms !== undefined && typeof values.rooms !== 'number') {
      values.rooms = Number(values.rooms);
    }
    if (values.bathrooms !== undefined && typeof values.bathrooms !== 'number') {
      values.bathrooms = Number(values.bathrooms);
    }
    if (values.buildYear !== undefined && typeof values.buildYear !== 'number') {
      values.buildYear = Number(values.buildYear);
    }
    values.createdAt = convertDateTimeToServer(values.createdAt);
    values.updatedAt = convertDateTimeToServer(values.updatedAt);

    const entity = {
      ...propertyEntity,
      ...values,
      complex: complexes.find(it => it.id.toString() === values.complex?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          createdAt: displayDefaultDateTime(),
          updatedAt: displayDefaultDateTime(),
        }
      : {
          ...propertyEntity,
          createdAt: convertDateTimeFromServer(propertyEntity.createdAt),
          updatedAt: convertDateTimeFromServer(propertyEntity.updatedAt),
          complex: propertyEntity?.complex?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="propserviceApp.propserviceProperty.home.createOrEditLabel" data-cy="PropertyCreateUpdateHeading">
            <Translate contentKey="propserviceApp.propserviceProperty.home.createOrEditLabel">Create or edit a Property</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="property-id"
                  label={translate('propserviceApp.propserviceProperty.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.address')}
                id="property-address"
                name="address"
                data-cy="address"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 255, message: translate('entity.validation.maxlength', { max: 255 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.regionCd')}
                id="property-regionCd"
                name="regionCd"
                data-cy="regionCd"
                type="text"
                validate={{
                  maxLength: { value: 255, message: translate('entity.validation.maxlength', { max: 255 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.localName')}
                id="property-localName"
                name="localName"
                data-cy="localName"
                type="text"
                validate={{
                  maxLength: { value: 255, message: translate('entity.validation.maxlength', { max: 255 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.street')}
                id="property-street"
                name="street"
                data-cy="street"
                type="text"
                validate={{
                  maxLength: { value: 255, message: translate('entity.validation.maxlength', { max: 255 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.floor')}
                id="property-floor"
                name="floor"
                data-cy="floor"
                type="text"
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.type')}
                id="property-type"
                name="type"
                data-cy="type"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 100, message: translate('entity.validation.maxlength', { max: 100 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.area')}
                id="property-area"
                name="area"
                data-cy="area"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.rooms')}
                id="property-rooms"
                name="rooms"
                data-cy="rooms"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.bathrooms')}
                id="property-bathrooms"
                name="bathrooms"
                data-cy="bathrooms"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.buildYear')}
                id="property-buildYear"
                name="buildYear"
                data-cy="buildYear"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.parkingYn')}
                id="property-parkingYn"
                name="parkingYn"
                data-cy="parkingYn"
                type="text"
                validate={{
                  minLength: { value: 1, message: translate('entity.validation.minlength', { min: 1 }) },
                  maxLength: { value: 1, message: translate('entity.validation.maxlength', { max: 1 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.description')}
                id="property-description"
                name="description"
                data-cy="description"
                type="text"
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.createdAt')}
                id="property-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceProperty.updatedAt')}
                id="property-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                id="property-complex"
                name="complex"
                data-cy="complex"
                label={translate('propserviceApp.propserviceProperty.complex')}
                type="select"
              >
                <option value="" key="0" />
                {complexes
                  ? complexes.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/propservice/property" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default PropertyUpdate;
