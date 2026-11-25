import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthApiError, AuthResponse, AuthUser, LoginRequest, RegisterRequest } from './auth.models';

const SESSION_KEY = 'gameharbor.auth.session';
const PRIVATE_ROUTES = ['/carrito', '/checkout', '/ordenes', '/biblioteca', '/cloud', '/recomendaciones'];

interface StoredSession { token: string; usuario: AuthUser; expiresAt: number; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly session = signal<StoredSession | null>(this.restoreSession());
  private expiryTimer: ReturnType<typeof setTimeout> | undefined;

  readonly currentUser = computed(() => this.session()?.usuario ?? null);
  readonly isAuthenticated = computed(() => this.session() !== null);

  constructor() {
    const session = this.session();
    if (session) this.scheduleExpiration(session.expiresAt);
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/api/auth/register`, request).pipe(
      // The real register endpoint returns AuthResponse with a signed JWT, so this session is based on its response.
      tap((response) => this.establishSession(response)),
      catchError((error: unknown) => throwError(() => this.toAuthError(error))),
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/api/auth/login`, request).pipe(
      tap((response) => this.establishSession(response)),
      catchError((error: unknown) => throwError(() => this.toAuthError(error))),
    );
  }

  getToken(): string | null {
    const session = this.session();
    if (!session) return null;
    if (session.expiresAt <= Date.now()) {
      this.expireSession();
      return null;
    }
    return session.token;
  }

  hasRole(role: 'CLIENTE' | 'ADMINISTRADOR'): boolean {
    return this.currentUser()?.roles.includes(role) ?? false;
  }

  logout(): void {
    this.clearSession();
  }

  handleUnauthorized(): void {
    const returnUrl = this.router.url;
    this.clearSession();
    void this.router.navigate(['/login'], { queryParams: { returnUrl } });
  }

  errorMessage(error: unknown): string {
    if (error instanceof AuthApiError) {
      const fieldMessages = Object.values(error.fieldErrors);
      if (error.status === 400 && fieldMessages.length) return fieldMessages.join(' ');
      return error.message;
    }
    return 'No se pudo completar la solicitud. IntÃ©ntalo nuevamente.';
  }

  private establishSession(response: AuthResponse): void {
    if (!response?.token || !response.usuario?.email || !Array.isArray(response.usuario.roles)) {
      throw new AuthApiError('El servidor devolviÃ³ una respuesta de autenticaciÃ³n incompleta.', 0);
    }
    const expiresAt = this.readExpiration(response.token);
    if (expiresAt <= Date.now()) throw new AuthApiError('El servidor devolviÃ³ un token vencido.', 0);

    const nextSession: StoredSession = { token: response.token, usuario: response.usuario, expiresAt };
    try {
      window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(nextSession));
    } catch {
      throw new AuthApiError('No se pudo guardar la sesiÃ³n en esta pestaÃ±a. Revisa la configuraciÃ³n de almacenamiento del navegador.', 0);
    }
    this.session.set(nextSession);
    this.scheduleExpiration(expiresAt);
  }

  private restoreSession(): StoredSession | null {
    try {
      const raw = window.sessionStorage.getItem(SESSION_KEY);
      if (!raw) return null;
      const value: unknown = JSON.parse(raw);
      if (!this.isStoredSession(value) || value.expiresAt <= Date.now() || this.readExpiration(value.token) <= Date.now()) {
        window.sessionStorage.removeItem(SESSION_KEY);
        return null;
      }
      return value;
    } catch {
      try { window.sessionStorage.removeItem(SESSION_KEY); } catch { /* Storage may be disabled by the browser. */ }
      return null;
    }
  }

  private isStoredSession(value: unknown): value is StoredSession {
    if (!value || typeof value !== 'object') return false;
    const candidate = value as Partial<StoredSession>;
    return typeof candidate.token === 'string'
      && typeof candidate.expiresAt === 'number'
      && !!candidate.usuario
      && typeof candidate.usuario.email === 'string'
      && Array.isArray(candidate.usuario.roles);
  }

  private readExpiration(token: string): number {
    try {
      const payloadPart = token.split('.')[1];
      if (!payloadPart) throw new Error('Missing JWT payload');
      const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
      const payload: unknown = JSON.parse(window.atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')));
      if (!payload || typeof payload !== 'object' || typeof (payload as { exp?: unknown }).exp !== 'number') {
        throw new Error('Missing JWT expiration');
      }
      // The client reads exp only for UX; the backend remains responsible for signature validation.
      return (payload as { exp: number }).exp * 1000;
    } catch {
      throw new AuthApiError('El servidor devolviÃ³ un token invÃ¡lido.', 0);
    }
  }

  private scheduleExpiration(expiresAt: number): void {
    if (this.expiryTimer) clearTimeout(this.expiryTimer);
    this.expiryTimer = setTimeout(() => {
      if (expiresAt <= Date.now()) this.expireSession();
      else this.scheduleExpiration(expiresAt);
    }, Math.min(Math.max(expiresAt - Date.now(), 0), 2_147_000_000));
  }

  private expireSession(): void {
    const currentUrl = this.router.url;
    this.clearSession();
    if (PRIVATE_ROUTES.some((path) => currentUrl.split('?')[0] === path || currentUrl.split('?')[0].startsWith(`${path}/`))) {
      void this.router.navigate(['/login'], { queryParams: { returnUrl: currentUrl } });
    }
  }

  private clearSession(): void {
    if (this.expiryTimer) clearTimeout(this.expiryTimer);
    this.expiryTimer = undefined;
    this.session.set(null);
    try { window.sessionStorage.removeItem(SESSION_KEY); } catch { /* Session state is still cleared in memory. */ }
  }

  private toAuthError(error: unknown): AuthApiError {
    if (error instanceof AuthApiError) return error;
    if (!(error instanceof HttpErrorResponse)) return new AuthApiError('No se pudo completar la solicitud.', 0);
    if (error.status === 0) return new AuthApiError('No se pudo conectar con GameHarbor. Comprueba que el backend estÃ© disponible.', 0);

    const body = error.error as { error?: unknown; detalle?: unknown } | null;
    const fieldErrors = this.readFieldErrors(body?.detalle);
    if (error.status === 401) return new AuthApiError('El correo o la contraseÃ±a no son correctos, o la cuenta estÃ¡ inactiva.', 401);
    if (error.status === 409) return new AuthApiError('Este correo ya estÃ¡ registrado.', 409);
    if (error.status === 400) return new AuthApiError(typeof body?.error === 'string' ? body.error : 'Revisa los datos ingresados.', 400, fieldErrors);
    if (error.status === 403) return new AuthApiError('No tienes permiso para realizar esta acciÃ³n.', 403);
    return new AuthApiError(typeof body?.error === 'string' ? body.error : 'OcurriÃ³ un error al comunicarse con GameHarbor.', error.status);
  }

  private readFieldErrors(value: unknown): Readonly<Record<string, string>> {
    if (!value || typeof value !== 'object' || Array.isArray(value)) return {};
    return Object.fromEntries(Object.entries(value).filter((entry): entry is [string, string] => typeof entry[1] === 'string'));
  }
}

