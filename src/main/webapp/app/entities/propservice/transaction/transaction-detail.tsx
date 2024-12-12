import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './transaction.reducer';

export const TransactionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const transactionEntity = useAppSelector(state => state.propservice.transaction.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="transactionDetailsHeading">
          <Translate contentKey="propserviceApp.propserviceTransaction.detail.title">Transaction</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="propserviceApp.propserviceTransaction.id">Id</Translate>
            </span>
          </dt>
          <dd>{transactionEntity.id}</dd>
          <dt>
            <span id="propertyId">
              <Translate contentKey="propserviceApp.propserviceTransaction.propertyId">Property Id</Translate>
            </span>
          </dt>
          <dd>{transactionEntity.propertyId}</dd>
          <dt>
            <span id="transactionType">
              <Translate contentKey="propserviceApp.propserviceTransaction.transactionType">Transaction Type</Translate>
            </span>
          </dt>
          <dd>{transactionEntity.transactionType}</dd>
          <dt>
            <span id="price">
              <Translate contentKey="propserviceApp.propserviceTransaction.price">Price</Translate>
            </span>
          </dt>
          <dd>{transactionEntity.price}</dd>
          <dt>
            <span id="transactionDate">
              <Translate contentKey="propserviceApp.propserviceTransaction.transactionDate">Transaction Date</Translate>
            </span>
          </dt>
          <dd>
            {transactionEntity.transactionDate ? (
              <TextFormat value={transactionEntity.transactionDate} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="buyer">
              <Translate contentKey="propserviceApp.propserviceTransaction.buyer">Buyer</Translate>
            </span>
          </dt>
          <dd>{transactionEntity.buyer}</dd>
          <dt>
            <span id="seller">
              <Translate contentKey="propserviceApp.propserviceTransaction.seller">Seller</Translate>
            </span>
          </dt>
          <dd>{transactionEntity.seller}</dd>
          <dt>
            <span id="agent">
              <Translate contentKey="propserviceApp.propserviceTransaction.agent">Agent</Translate>
            </span>
          </dt>
          <dd>{transactionEntity.agent}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="propserviceApp.propserviceTransaction.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>
            {transactionEntity.createdAt ? <TextFormat value={transactionEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="updatedAt">
              <Translate contentKey="propserviceApp.propserviceTransaction.updatedAt">Updated At</Translate>
            </span>
          </dt>
          <dd>
            {transactionEntity.updatedAt ? <TextFormat value={transactionEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
        </dl>
        <Button tag={Link} to="/propservice/transaction" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/propservice/transaction/${transactionEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default TransactionDetail;
