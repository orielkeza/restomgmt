import { authFetch } from './authFetch';

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  enabled: boolean;
}

export const userApi = {
  getAllUsers: (token: string | null): Promise<UserResponse[]> =>
    authFetch('/users', token),

  getUserById: (token: string | null, id: number): Promise<UserResponse> =>
    authFetch(`/users/${id}/info`, token),

  updateUser: (token: string | null, id: number, fullName: string, email: string): Promise<UserResponse> =>
    authFetch(`/users/${id}/update`, token, { method: 'PUT', body: JSON.stringify({ fullName, email }) }),

  deleteUser: (token: string | null, id: number): Promise<void> =>
    authFetch(`/users/${id}/delete`, token, { method: 'DELETE' }),

  assignRole: (token: string | null, id: number, roleName: string): Promise<UserResponse> =>
    authFetch(`/users/${id}/roles`, token, { method: 'PUT', body: JSON.stringify({ roleName }) }),
};