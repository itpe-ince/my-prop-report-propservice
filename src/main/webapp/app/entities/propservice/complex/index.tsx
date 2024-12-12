import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Complex from './complex';
import ComplexDetail from './complex-detail';
import ComplexUpdate from './complex-update';
import ComplexDeleteDialog from './complex-delete-dialog';

const ComplexRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Complex />} />
    <Route path="new" element={<ComplexUpdate />} />
    <Route path=":id">
      <Route index element={<ComplexDetail />} />
      <Route path="edit" element={<ComplexUpdate />} />
      <Route path="delete" element={<ComplexDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ComplexRoutes;
