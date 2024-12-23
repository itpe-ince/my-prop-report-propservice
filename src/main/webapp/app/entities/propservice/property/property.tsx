import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Col, Form, FormGroup, Input, InputGroup, Row, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, getPaginationState, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities, searchEntities } from './property.reducer';

export const Property = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [search, setSearch] = useState('');
  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const propertyList = useAppSelector(state => state.propservice.property.entities);
  const loading = useAppSelector(state => state.propservice.property.loading);
  const totalItems = useAppSelector(state => state.propservice.property.totalItems);

  const getAllEntities = () => {
    if (search) {
      dispatch(
        searchEntities({
          query: search,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        }),
      );
    } else {
      dispatch(
        getEntities({
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        }),
      );
    }
  };

  const startSearching = e => {
    if (search) {
      setPaginationState({
        ...paginationState,
        activePage: 1,
      });
      dispatch(
        searchEntities({
          query: search,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        }),
      );
    }
    e.preventDefault();
  };

  const clear = () => {
    setSearch('');
    setPaginationState({
      ...paginationState,
      activePage: 1,
    });
    dispatch(getEntities({}));
  };

  const handleSearch = event => setSearch(event.target.value);

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort, search]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const order = paginationState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="property-heading" data-cy="PropertyHeading">
        <Translate contentKey="propserviceApp.propserviceProperty.home.title">Properties</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="propserviceApp.propserviceProperty.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link
            to="/propservice/property/new"
            className="btn btn-primary jh-create-entity"
            id="jh-create-entity"
            data-cy="entityCreateButton"
          >
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="propserviceApp.propserviceProperty.home.createLabel">Create new Property</Translate>
          </Link>
        </div>
      </h2>
      <Row>
        <Col sm="12">
          <Form onSubmit={startSearching}>
            <FormGroup>
              <InputGroup>
                <Input
                  type="text"
                  name="search"
                  defaultValue={search}
                  onChange={handleSearch}
                  placeholder={translate('propserviceApp.propserviceProperty.home.search')}
                />
                <Button className="input-group-addon">
                  <FontAwesomeIcon icon="search" />
                </Button>
                <Button type="reset" className="input-group-addon" onClick={clear}>
                  <FontAwesomeIcon icon="trash" />
                </Button>
              </InputGroup>
            </FormGroup>
          </Form>
        </Col>
      </Row>
      <div className="table-responsive">
        {propertyList && propertyList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.id">Id</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('address')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.address">Address</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('address')} />
                </th>
                <th className="hand" onClick={sort('regionCd')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.regionCd">Region Cd</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('regionCd')} />
                </th>
                <th className="hand" onClick={sort('localName')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.localName">Local Name</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('localName')} />
                </th>
                <th className="hand" onClick={sort('street')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.street">Street</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('street')} />
                </th>
                <th className="hand" onClick={sort('floor')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.floor">Floor</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('floor')} />
                </th>
                <th className="hand" onClick={sort('type')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.type">Type</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('type')} />
                </th>
                <th className="hand" onClick={sort('area')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.area">Area</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('area')} />
                </th>
                <th className="hand" onClick={sort('rooms')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.rooms">Rooms</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('rooms')} />
                </th>
                <th className="hand" onClick={sort('bathrooms')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.bathrooms">Bathrooms</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('bathrooms')} />
                </th>
                <th className="hand" onClick={sort('buildYear')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.buildYear">Build Year</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('buildYear')} />
                </th>
                <th className="hand" onClick={sort('parkingYn')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.parkingYn">Parking Yn</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('parkingYn')} />
                </th>
                <th className="hand" onClick={sort('description')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.description">Description</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('description')} />
                </th>
                <th className="hand" onClick={sort('createdAt')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.createdAt">Created At</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('createdAt')} />
                </th>
                <th className="hand" onClick={sort('updatedAt')}>
                  <Translate contentKey="propserviceApp.propserviceProperty.updatedAt">Updated At</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('updatedAt')} />
                </th>
                <th>
                  <Translate contentKey="propserviceApp.propserviceProperty.complex">Complex</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {propertyList.map((property, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/propservice/property/${property.id}`} color="link" size="sm">
                      {property.id}
                    </Button>
                  </td>
                  <td>{property.address}</td>
                  <td>{property.regionCd}</td>
                  <td>{property.localName}</td>
                  <td>{property.street}</td>
                  <td>{property.floor}</td>
                  <td>{property.type}</td>
                  <td>{property.area}</td>
                  <td>{property.rooms}</td>
                  <td>{property.bathrooms}</td>
                  <td>{property.buildYear}</td>
                  <td>{property.parkingYn}</td>
                  <td>{property.description}</td>
                  <td>{property.createdAt ? <TextFormat type="date" value={property.createdAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{property.updatedAt ? <TextFormat type="date" value={property.updatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{property.complex ? <Link to={`/propservice/complex/${property.complex.id}`}>{property.complex.id}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/propservice/property/${property.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/propservice/property/${property.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() =>
                          (window.location.href = `/propservice/property/${property.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="propserviceApp.propserviceProperty.home.notFound">No Properties found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={propertyList && propertyList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default Property;
