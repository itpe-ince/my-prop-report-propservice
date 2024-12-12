import dayjs from 'dayjs';

export interface IComplex {
  id?: number;
  complexName?: string;
  state?: string | null;
  county?: string | null;
  city?: string | null;
  town?: string | null;
  addressCode?: string | null;
  createdAt?: dayjs.Dayjs;
  updatedAt?: dayjs.Dayjs | null;
}

export const defaultValue: Readonly<IComplex> = {};
