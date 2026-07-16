import { API_BASE_URL } from './config';

export interface CategoryResponse {
  id: number;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface MenuItemResponse {
  id: number;
  name: string;
  description: string | null;
  cost: number;           // BigDecimal serializes as a JSON number by default
  available: boolean;
  categoryName: string;
  createdAt: string;
  updatedAt: string;
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    throw new Error(`API error ${res.status}: ${res.statusText}`);
  }
  return res.json() as Promise<T>;
}

export const menuApi = {
  // GET /menu/items — public, returns only available items (per your controller)
  getAvailableItems: (): Promise<MenuItemResponse[]> =>
    fetch(`${API_BASE_URL}/menu/items`).then(handleResponse),

  // GET /menu/items/all — requires ADMIN/STAFF auth
  getAllItems: (token: string): Promise<MenuItemResponse[]> =>
    fetch(`${API_BASE_URL}/menu/items/all`, {
      headers: { Authorization: `Bearer ${token}` },
    }).then(handleResponse),

  // GET /menu/categories — public
  getCategories: (): Promise<CategoryResponse[]> =>
    fetch(`${API_BASE_URL}/menu/categories`).then(handleResponse),

  // PATCH /menu/items/{id}/availability — requires ADMIN/STAFF auth
  toggleAvailability: (id: number, token: string): Promise<MenuItemResponse> =>
    fetch(`${API_BASE_URL}/menu/items/${id}/availability`, {
      method: 'PATCH',
      headers: { Authorization: `Bearer ${token}` },
    }).then(handleResponse),
};