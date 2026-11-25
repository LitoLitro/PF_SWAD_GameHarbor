import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, RouterStateSnapshot, ActivatedRouteSnapshot, UrlTree } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { authGuard } from './auth.guard';

function makeToken(): string {
  const exp = Math.floor(Date.now() / 1000) + 600;
  const payload = btoa(JSON.stringify({ exp })).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  return `header.${payload}.signature`;
}

describe('authGuard', () => {
  beforeEach(() => { sessionStorage.clear(); TestBed.configureTestingModule({ providers: [provideRouter([])] }); });
  afterEach(() => sessionStorage.clear());

  it('redirects unauthenticated navigation to login with the requested return URL', () => {
    const result = TestBed.runInInjectionContext(() => authGuard({} as ActivatedRouteSnapshot, { url: '/biblioteca' } as RouterStateSnapshot));
    expect(result instanceof UrlTree).toBe(true);
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toContain('returnUrl=%2Fbiblioteca');
  });

  it('allows navigation with a valid stored session', () => {
    const token = makeToken();
    const exp = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).exp;
    sessionStorage.setItem('gameharbor.auth.session', JSON.stringify({ token, expiresAt: exp * 1000, usuario: { id: 7, nombre: 'QA', email: 'qa@gameharbor.test', saldo: 0, roles: ['CLIENTE'] } }));
    expect(TestBed.runInInjectionContext(() => authGuard({} as ActivatedRouteSnapshot, { url: '/carrito' } as RouterStateSnapshot))).toBe(true);
  });
});
