import { authFetch } from './authFetch';

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'OUTFORDELIVERY' | 'DELIVERED' | 'CANCELLED';

export interface OrderItemResponse {
  menuItemId: number;
  itemName: string;
  priceAtOrder: number;
  quantity: number;
  subtotal: number;
}

export interface OrderResponse {
  orderId: number;
  username: string;
  status: OrderStatus;
  items: OrderItemResponse[];
  total: number;
  warnings: string[];
  riderPhone: string | null;
  deliveryNote: string | null;
  createdAt: string;
  updatedAt: string;
}

export const orderApi = {
  placeOrder: (token: string | null): Promise<OrderResponse> =>
    authFetch('/orders', token, { method: 'POST' }),

  getMyOrders: (token: string | null): Promise<OrderResponse[]> =>
    authFetch('/orders', token),

  getOrderById: (token: string | null, id: number): Promise<OrderResponse> =>
    authFetch(`/orders/${id}`, token),

  cancelOrder: (token: string | null, id: number): Promise<OrderResponse> =>
    authFetch(`/orders/${id}/cancel`, token, { method: 'DELETE' }),

  // ADMIN/STAFF only
  getAllOrders: (token: string | null): Promise<OrderResponse[]> =>
    authFetch('/orders/all', token),

  advanceOrderStatus: (token: string | null, id: number, status: OrderStatus): Promise<OrderResponse> =>
    authFetch(`/orders/${id}/status`, token, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    }),

  assignRider: (token: string | null, id: number, riderPhone: string, deliveryNote?: string): Promise<OrderResponse> =>
    authFetch(`/orders/${id}/rider`, token, {
      method: 'PUT',
      body: JSON.stringify({ riderPhone, deliveryNote }),
    }),
};