import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './transaction.reducer';

export const TransactionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const transactionEntity = useAppSelector(state => state.propservice.transaction.entity);
  const loading = useAppSelector(state => state.propservice.transaction.loading);
  const updating = useAppSelector(state => state.propservice.transaction.updating);
  const updateSuccess = useAppSelector(state => state.propservice.transaction.updateSuccess);

  const handleClose = () => {
    navigate(`/propservice/transaction${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
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
    if (values.propertyId !== undefined && typeof values.propertyId !== 'number') {
      values.propertyId = Number(values.propertyId);
    }
    if (values.price !== undefined && typeof values.price !== 'number') {
      values.price = Number(values.price);
    }
    values.transactionDate = convertDateTimeToServer(values.transactionDate);
    values.createdAt = convertDateTimeToServer(values.createdAt);
    values.updatedAt = convertDateTimeToServer(values.updatedAt);

    const entity = {
      ...transactionEntity,
      ...values,
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
          transactionDate: displayDefaultDateTime(),
          createdAt: displayDefaultDateTime(),
          updatedAt: displayDefaultDateTime(),
        }
      : {
          ...transactionEntity,
          transactionDate: convertDateTimeFromServer(transactionEntity.transactionDate),
          createdAt: convertDateTimeFromServer(transactionEntity.createdAt),
          updatedAt: convertDateTimeFromServer(transactionEntity.updatedAt),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="propserviceApp.propserviceTransaction.home.createOrEditLabel" data-cy="TransactionCreateUpdateHeading">
            <Translate contentKey="propserviceApp.propserviceTransaction.home.createOrEditLabel">Create or edit a Transaction</Translate>
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
                  id="transaction-id"
                  label={translate('propserviceApp.propserviceTransaction.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('propserviceApp.propserviceTransaction.propertyId')}
                id="transaction-propertyId"
                name="propertyId"
                data-cy="propertyId"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceTransaction.transactionType')}
                id="transaction-transactionType"
                name="transactionType"
                data-cy="transactionType"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  maxLength: { value: 50, message: translate('entity.validation.maxlength', { max: 50 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceTransaction.price')}
                id="transaction-price"
                name="price"
                data-cy="price"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceTransaction.transactionDate')}
                id="transaction-transactionDate"
                name="transactionDate"
                data-cy="transactionDate"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceTransaction.buyer')}
                id="transaction-buyer"
                name="buyer"
                data-cy="buyer"
                type="text"
                validate={{
                  maxLength: { value: 100, message: translate('entity.validation.maxlength', { max: 100 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceTransaction.seller')}
                id="transaction-seller"
                name="seller"
                data-cy="seller"
                type="text"
                validate={{
                  maxLength: { value: 100, message: translate('entity.validation.maxlength', { max: 100 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceTransaction.agent')}
                id="transaction-agent"
                name="agent"
                data-cy="agent"
                type="text"
                validate={{
                  maxLength: { value: 100, message: translate('entity.validation.maxlength', { max: 100 }) },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceTransaction.createdAt')}
                id="transaction-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('propserviceApp.propserviceTransaction.updatedAt')}
                id="transaction-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/propservice/transaction" replace color="info">
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

export default TransactionUpdate;
