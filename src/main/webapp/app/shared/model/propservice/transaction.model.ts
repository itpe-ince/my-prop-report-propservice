import dayjs from 'dayjs';

export interface ITransaction {
  id?: number;
  propertyId?: number;
  transactionType?: string;
  price?: number;
  transactionDate?: dayjs.Dayjs;
  buyer?: string | null;
  seller?: string | null;
  agent?: string | null;
  createdAt?: dayjs.Dayjs;
  updatedAt?: dayjs.Dayjs | null;
}

export const defaultValue: Readonly<ITransaction> = {};
