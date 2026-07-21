import { authFetch } from './authFetch';

export type PaymentStatus = 'PENDING' | 'SUCCESSFUL' | 'FAILED';

export interface PaymentResponse {
  paymentId: number;
  orderId: number;
  momoReferenceId: string;
  amount: number;
  payerPhone: string;
  status: PaymentStatus;
  failureReason: string | null;
  refundFlagged: boolean;
  createdAt: string;
  updatedAt: string;
}

export const paymentApi = {
  initiatePayment: (token: string | null, orderId: number, payerPhone: string): Promise<PaymentResponse> =>
    authFetch(`/payments/orders/${orderId}`, token, {
      method: 'POST',
      body: JSON.stringify({ payerPhone }),
    }),

  checkStatus: (token: string | null, orderId: number): Promise<PaymentResponse> =>
    authFetch(`/payments/orders/${orderId}/status`, token),

  getPayment: (token: string | null, orderId: number): Promise<PaymentResponse> =>
    authFetch(`/payments/orders/${orderId}`, token),

  // ADMIN/STAFF only
  flagRefund: (token: string | null, orderId: number): Promise<PaymentResponse> =>
    authFetch(`/payments/orders/${orderId}/refund`, token, { method: 'POST' }),
};