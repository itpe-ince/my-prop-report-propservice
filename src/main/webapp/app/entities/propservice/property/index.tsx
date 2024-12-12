import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Property from './property';
import PropertyDetail from './property-detail';
import PropertyUpdate from './property-update';
import PropertyDeleteDialog from './property-delete-dialog';

const PropertyRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Property />} />
    <Route path="new" element={<PropertyUpdate />} />
    <Route path=":id">
      <Route index element={<PropertyDetail />} />
      <Route path="edit" element={<PropertyUpdate />} />
      <Route path="delete" element={<PropertyDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PropertyRoutes;
