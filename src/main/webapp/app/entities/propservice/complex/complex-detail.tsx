import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './complex.reducer';

export const ComplexDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const complexEntity = useAppSelector(state => state.propservice.complex.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="complexDetailsHeading">
          <Translate contentKey="propserviceApp.propserviceComplex.detail.title">Complex</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="propserviceApp.propserviceComplex.id">Id</Translate>
            </span>
          </dt>
          <dd>{complexEntity.id}</dd>
          <dt>
            <span id="complexName">
              <Translate contentKey="propserviceApp.propserviceComplex.complexName">Complex Name</Translate>
            </span>
          </dt>
          <dd>{complexEntity.complexName}</dd>
          <dt>
            <span id="state">
              <Translate contentKey="propserviceApp.propserviceComplex.state">State</Translate>
            </span>
          </dt>
          <dd>{complexEntity.state}</dd>
          <dt>
            <span id="county">
              <Translate contentKey="propserviceApp.propserviceComplex.county">County</Translate>
            </span>
          </dt>
          <dd>{complexEntity.county}</dd>
          <dt>
            <span id="city">
              <Translate contentKey="propserviceApp.propserviceComplex.city">City</Translate>
            </span>
          </dt>
          <dd>{complexEntity.city}</dd>
          <dt>
            <span id="town">
              <Translate contentKey="propserviceApp.propserviceComplex.town">Town</Translate>
            </span>
          </dt>
          <dd>{complexEntity.town}</dd>
          <dt>
            <span id="addressCode">
              <Translate contentKey="propserviceApp.propserviceComplex.addressCode">Address Code</Translate>
            </span>
          </dt>
          <dd>{complexEntity.addressCode}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="propserviceApp.propserviceComplex.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>{complexEntity.createdAt ? <TextFormat value={complexEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="updatedAt">
              <Translate contentKey="propserviceApp.propserviceComplex.updatedAt">Updated At</Translate>
            </span>
          </dt>
          <dd>{complexEntity.updatedAt ? <TextFormat value={complexEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
        </dl>
        <Button tag={Link} to="/propservice/complex" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/propservice/complex/${complexEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ComplexDetail;
