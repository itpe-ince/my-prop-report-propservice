import dayjs from 'dayjs';
import { IComplex } from 'app/shared/model/propservice/complex.model';

export interface IProperty {
  id?: number;
  complexId?: number;
  address?: string;
  regionCd?: string | null;
  localName?: string | null;
  street?: string | null;
  floor?: number | null;
  type?: string;
  area?: number;
  rooms?: number;
  bathrooms?: number;
  buildYear?: number;
  parkingYn?: string | null;
  description?: string | null;
  createdAt?: dayjs.Dayjs;
  updatedAt?: dayjs.Dayjs | null;
  complex?: IComplex | null;
}

export const defaultValue: Readonly<IProperty> = {};
