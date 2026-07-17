import { API_BASE_URL } from './config';

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

// Wraps fetch with the Authorization header and consistent error handling.
// Pass the token explicitly rather than reading Redux here — keeps this file
// framework-agnostic and testable outside a store.
export async function authFetch<T>(
  path: string,
  token: string | null,
  options: RequestInit = {}
): Promise<T> {
  if (!token) {
    throw new ApiError('Not authenticated', 401);
  }

  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
      ...options.headers,
    },
  });

  // 204 No Content — nothing to parse
  if (res.status === 204) {
    return undefined as T;
  }

  const text = await res.text();
  let body: unknown = text;
  try {
    body = text ? JSON.parse(text) : undefined;
  } catch {
    // leave as raw text (some error responses are plain strings)
  }

  if (!res.ok) {
    const message = typeof body === 'string' ? body : (body as { message?: string })?.message;
    throw new ApiError(message ?? `Request failed (${res.status})`, res.status);
  }

  return body as T;
}