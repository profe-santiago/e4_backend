const REFRESH_TOKEN_KEY = 'auth_refresh_token'

export const RefreshTokenStorage = {
  get: (): string | null => localStorage.getItem(REFRESH_TOKEN_KEY),
  set: (token: string): void => localStorage.setItem(REFRESH_TOKEN_KEY, token),
  remove: (): void => localStorage.removeItem(REFRESH_TOKEN_KEY),
}
