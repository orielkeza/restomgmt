import { authFetch } from './authFetch';
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
  cost: number;
  available: boolean;
  categoryName: string;
  createdAt: string;
  updatedAt: string;
}

export interface MenuItemRequest {
  name: string;
  description?: string;
  cost: number;
  categoryId: number;
  available: boolean;
}

export interface CategoryRequest {
  name: string;
}

async function handlePublicResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    throw new Error(`API error ${res.status}: ${res.statusText}`);
  }
  return res.json() as Promise<T>;
}

export const menuApi = {
  // ---- Public reads (no auth required) ----
  getAvailableItems: (): Promise<MenuItemResponse[]> =>
    fetch(`${API_BASE_URL}/menu/items`).then(handlePublicResponse<MenuItemResponse[]>),

  getCategories: (): Promise<CategoryResponse[]> =>
    fetch(`${API_BASE_URL}/menu/categories`).then(handlePublicResponse<CategoryResponse[]>),

  getItemById: (id: number): Promise<MenuItemResponse> =>
    fetch(`${API_BASE_URL}/menu/items/${id}`).then(handlePublicResponse<MenuItemResponse>),
  // ---- ADMIN/STAFF only (require token) ----
  getAllItems: (token: string | null): Promise<MenuItemResponse[]> =>
    authFetch('/menu/items/all', token),

  toggleAvailability: (id: number, token: string | null): Promise<MenuItemResponse> =>
    authFetch(`/menu/items/${id}/availability`, token, { method: 'PATCH' }),

  createItem: (token: string | null, payload: MenuItemRequest): Promise<MenuItemResponse> =>
    authFetch('/menu/items', token, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  updateItem: (token: string | null, id: number, payload: MenuItemRequest): Promise<MenuItemResponse> =>
    authFetch(`/menu/items/${id}`, token, {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),

  deleteItem: (token: string | null, id: number): Promise<void> =>
    authFetch(`/menu/items/${id}`, token, { method: 'DELETE' }),

  createCategory: (token: string | null, payload: CategoryRequest): Promise<CategoryResponse> =>
    authFetch('/menu/categories', token, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  updateCategory: (token: string | null, id: number, payload: CategoryRequest): Promise<CategoryResponse> =>
    authFetch(`/menu/categories/${id}`, token, {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),

  deleteCategory: (token: string | null, id: number): Promise<void> =>
    authFetch(`/menu/categories/${id}`, token, { method: 'DELETE' }),
};