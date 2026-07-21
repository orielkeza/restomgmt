import { API_BASE_URL } from './config';

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
  fullName: string;
}

async function readBody(res: Response): Promise<string> {
  // backend returns either a raw JWT string or a plain text message —
  // both come back as text, never structured JSON, for these endpoints
  const text = await res.text();
  // login's token is JSON-stringified (ResponseEntity.ok(token) -> "\"eyJ...\""), strip quotes if present
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export const authApi = {
  login: async (username: string, password: string): Promise<string> => {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
    const body = await readBody(res);
    if (!res.ok) {
      // body is the error message string, e.g. "Email not verified. Please check your inbox."
      throw new Error(body || 'Login failed');
    }
    return body; // the JWT
  },

  register: async (payload: RegisterPayload): Promise<string> => {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    const body = await readBody(res);
    if (!res.ok) {
      throw new Error(body || 'Registration failed');
    }
    return body; // "Registration successful. Please verify your email."
  },

  resendVerification: async (email: string): Promise<string> => {
    const res = await fetch(`${API_BASE_URL}/auth/resend-verification`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });
    const body = await readBody(res);
    if (!res.ok) throw new Error(body || 'Could not resend verification email');
    return body;
  },

  forgotPassword: async (email: string): Promise<string> => {
      const res = await fetch(`${API_BASE_URL}/auth/forgot-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      });
      const body = await readBody(res);
      if (!res.ok) throw new Error(body || 'Request failed');
      return body;
  },

  resetPassword: async (token: string, newPassword: string): Promise<string> => {
      const res = await fetch(`${API_BASE_URL}/auth/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword }),
      });
      const body = await readBody(res);
      if (!res.ok) throw new Error(body || 'Reset failed');
      return body;
  },

  verifyEmail: async (token: string): Promise<string> => {
      const res = await fetch(`${API_BASE_URL}/auth/verify-email?token=${encodeURIComponent(token)}`);
      const body = await readBody(res);
      if (!res.ok) throw new Error(body || 'Verification failed');
      return body;
  },
};