import { authFetch } from './authFetch';

export interface CartItemResponse {
  menuItemId: number;
  itemName: string;
  itemPrice: number;
  quantity: number;
  subtotal: number;
}

export interface CartResponse {
  cartId: number;
  username: string;
  items: CartItemResponse[];
  total: number;
  createdAt: string;
  updatedAt: string;
}

export const cartApi = {
  getCart: (token: string | null): Promise<CartResponse> =>
    authFetch('/cart', token),

  addItem: (token: string | null, menuItemId: number, quantity: number): Promise<CartResponse> =>
    authFetch('/cart/items', token, {
      method: 'POST',
      body: JSON.stringify({ menuItemId, quantity }),
    }),

  updateItemQuantity: (token: string | null, menuItemId: number, quantity: number): Promise<CartResponse> =>
    authFetch(`/cart/items/${menuItemId}`, token, {
      method: 'PUT',
      body: JSON.stringify({ quantity }),
    }),

  removeItem: (token: string | null, menuItemId: number): Promise<CartResponse> =>
    authFetch(`/cart/items/${menuItemId}`, token, { method: 'DELETE' }),

  clearCart: (token: string | null): Promise<void> =>
    authFetch('/cart', token, { method: 'DELETE' }),
};