interface DecodedToken {
  sub: string;
  roles: string[];
  exp: number;
}

export function decodeJwt(token: string): DecodedToken | null {
  try {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
    return decoded as DecodedToken;
  } catch {
    return null;
  }
}

export function isTokenExpired(token: string): boolean {
  const decoded = decodeJwt(token);
  if (!decoded) return true;
  return decoded.exp * 1000 < Date.now();
}