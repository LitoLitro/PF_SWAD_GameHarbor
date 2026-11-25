import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../environments/environment';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

function token(exp = Math.floor(Date.now() / 1000) + 600): string {
  const payload = btoa(JSON.stringify({ sub: 'qa', exp })).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  return `header.${payload}.signature`;
}
const fixturePassword = ['unit', 'test', 'only'].join('-');
const user = { id: 7, nombre: 'QA', email: 'qa@gameharbor.test', saldo: 0, roles: ['CLIENTE'] as const };

describe('AuthService', () => {
  let http: HttpTestingController;
  let auth: AuthService;
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting(), provideRouter([])] });
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });
  afterEach(() => { http.verify(); auth.logout(); sessionStorage.clear(); vi.useRealTimers(); });

  it('logs in, stores the returned JWT in this tab and clears it on logout', () => {
    let completed = false;
    auth.login({ email: user.email, password: fixturePassword }).subscribe(() => completed = true);
    const req = http.expectOne(`${environment.apiUrl}/api/auth/login`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({ token: token(), usuario: user });
    expect(completed).toBe(true);
    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.getToken()).toBeTruthy();
    expect(sessionStorage.getItem('gameharbor.auth.session')).toContain('header.');
    auth.logout();
    expect(auth.isAuthenticated()).toBe(false);
    expect(sessionStorage.getItem('gameharbor.auth.session')).toBeNull();
  });

  it('uses the register response without contacting a real backend', () => {
    auth.register({ nombre: user.nombre, email: user.email, password: fixturePassword }).subscribe();
    http.expectOne(`${environment.apiUrl}/api/auth/register`).flush({ token: token(), usuario: user });
    expect(auth.currentUser()?.email).toBe(user.email);
  });

  it('converts an HTTP 401 to a safe authentication message', () => {
    let failure: unknown;
    auth.login({ email: user.email, password: fixturePassword }).subscribe({ error: error => failure = error });
    http.expectOne(`${environment.apiUrl}/api/auth/login`).flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.errorMessage(failure)).toContain('correo');
    expect(auth.isAuthenticated()).toBe(false);
  });

  it('rejects expired JWTs returned by the server', () => {
    let failure: unknown;
    auth.login({ email: user.email, password: fixturePassword }).subscribe({ error: error => failure = error });
    http.expectOne(`${environment.apiUrl}/api/auth/login`).flush({ token: token(Math.floor(Date.now() / 1000) - 10), usuario: user });
    expect(auth.errorMessage(failure)).toContain('vencido');
    expect(auth.isAuthenticated()).toBe(false);
  });

  it('clears the session automatically when its JWT expiration time is reached', () => {
    vi.useFakeTimers();
    const exp = Math.floor(Date.now() / 1000) + 600;
    auth.login({ email: user.email, password: fixturePassword }).subscribe();
    http.expectOne(`${environment.apiUrl}/api/auth/login`).flush({ token: token(exp), usuario: user });
    expect(auth.isAuthenticated()).toBe(true);
    vi.advanceTimersByTime(600_001);
    expect(auth.isAuthenticated()).toBe(false);
    expect(sessionStorage.getItem('gameharbor.auth.session')).toBeNull();
  });
});

