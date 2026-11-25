import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../environments/environment';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

@Component({ standalone: true, template: '' }) class LoginTarget {}

function establishSession(): string {
  const exp = Math.floor(Date.now() / 1000) + 600;
  const payload = btoa(JSON.stringify({ exp })).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  const token = `header.${payload}.signature`;
  sessionStorage.setItem('gameharbor.auth.session', JSON.stringify({ token, expiresAt: exp * 1000, usuario: { id: 7, nombre: 'QA', email: 'qa@gameharbor.test', saldo: 0, roles: ['CLIENTE'] } }));
  return token;
}

describe('JWT interceptor', () => {
  let http: HttpTestingController;
  let client: HttpClient;
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting(), provideRouter([{ path: 'login', component: LoginTarget }])] });
    http = TestBed.inject(HttpTestingController); client = TestBed.inject(HttpClient);
  });
  afterEach(() => { http.verify(); TestBed.inject(AuthService).logout(); sessionStorage.clear(); });

  it('adds Bearer JWT to protected routes only', () => {
    const jwt = establishSession();
    client.get('/api/carrito').subscribe();
    expect(http.expectOne('/api/carrito').request.headers.get('Authorization')).toBe(`Bearer ${jwt}`);
    client.get(`${environment.apiUrl}/api/juegos`).subscribe();
    expect(http.expectOne(`${environment.apiUrl}/api/juegos`).request.headers.has('Authorization')).toBe(false);
  });

  it('clears the local session after 401 and preserves it after 403', () => {
    establishSession();
    client.get('/api/ordenes').subscribe({ error: () => undefined });
    http.expectOne('/api/ordenes').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(sessionStorage.getItem('gameharbor.auth.session')).toBeNull();

    establishSession();
    client.post('/api/juegos/1/resenas', { calificacion: 5, comentario: 'test' }).subscribe({ error: () => undefined });
    http.expectOne('/api/juegos/1/resenas').flush({}, { status: 403, statusText: 'Forbidden' });
    expect(sessionStorage.getItem('gameharbor.auth.session')).not.toBeNull();
  });
});
