import { Component } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../environments/environment';
import { AuthService } from '../core/auth/auth.service';
import { CatalogPageComponent, apiMessage } from './catalog/catalog-page.component';
import { GameDetailPageComponent } from './game-detail/game-detail-page.component';
import { HomePageComponent } from './home/home-page.component';
import { LoginPageComponent } from './auth/login-page.component';
import { RegisterPageComponent } from './auth/register-page.component';

@Component({ standalone: true, template: '' }) class TestHome {}
const api = `${environment.apiUrl}/api`;
const fixturePassword = ['unit', 'test', 'only'].join('-');
const game = { id: 2, titulo: 'QA Fixture Game', precio: 12.5, imagenUrl: null, requiereCloud: false, desarrollador: 'Fixture Studio', categorias: ['Aventura'] };
const page = { contenido: [game], pagina: 0, tamano: 12, totalElementos: 1, totalPaginas: 1 };
function jwt(): string {
  const exp = Math.floor(Date.now() / 1000) + 600;
  const payload = btoa(JSON.stringify({ exp })).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  return `header.${payload}.signature`;
}
const authResponse = { token: jwt(), usuario: { id: 7, nombre: 'QA', email: 'qa@gameharbor.test', saldo: 0, roles: ['CLIENTE'] } };

describe('main Angular pages', () => {
  let http: HttpTestingController;
  beforeEach(() => { sessionStorage.clear(); TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '', component: TestHome }])] }); http = TestBed.inject(HttpTestingController); });
  afterEach(() => { http.verify(); sessionStorage.clear(); });

  it('renders home with live catalog data returned by the API mock', () => {
    const fixture = TestBed.createComponent(HomePageComponent); fixture.detectChanges();
    http.expectOne(r => r.url === `${api}/juegos` && r.params.get('tamano') === '3').flush(page);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('QA Fixture Game');
    expect(fixture.nativeElement.textContent).not.toContain('Cyberpunk Simulation');
  });

  it('renders catalog games and obtains categories through separate API requests', () => {
    const fixture = TestBed.createComponent(CatalogPageComponent); fixture.detectChanges();
    http.expectOne(`${api}/categorias`).flush([{ id: 1, nombre: 'Aventura', descripcion: '' }]);
    http.expectOne(r => r.url === `${api}/juegos` && r.params.get('pagina') === '0').flush(page);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('QA Fixture Game');
    expect(fixture.nativeElement.querySelector('select option')?.textContent).toContain('Todas');
  });

  it('renders the game detail and public review response', async () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [GameDetailPageComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '2' })) } },
        { provide: AuthService, useValue: { isAuthenticated: () => false } }],
    });
    http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(GameDetailPageComponent); fixture.detectChanges();
    http.expectOne(`${api}/juegos/2`).flush({ ...game, descripcion: 'Fixture detail', fechaLanzamiento: '2026-01-01', estado: 'DISPONIBLE', calificacionPromedio: null, totalResenas: 0 });
    http.expectOne(`${api}/juegos/2/resenas`).flush([]);
    await fixture.whenStable(); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('QA Fixture Game');
    expect(fixture.nativeElement.textContent).toContain('Fixture detail');
  });
});

describe('authentication forms and API errors', () => {
  let http: HttpTestingController;
  beforeEach(() => { sessionStorage.clear(); TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: '', component: TestHome }])] }); http = TestBed.inject(HttpTestingController); });
  afterEach(() => { http.verify(); sessionStorage.clear(); });

  it('validates login fields locally and presents the backend 401 message', () => {
    const fixture = TestBed.createComponent(LoginPageComponent); fixture.detectChanges();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Ingresa tu correo');
    http.expectNone(`${api}/auth/login`);

    const email = fixture.nativeElement.querySelector('#login-email') as HTMLInputElement;
    const password = fixture.nativeElement.querySelector('#login-password') as HTMLInputElement;
    email.value = 'qa@gameharbor.test'; email.dispatchEvent(new Event('input', { bubbles: true }));
    password.value = fixturePassword; password.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    http.expectOne(`${api}/auth/login`).flush({}, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain('correo');
  });

  it('validates registration without submitting and handles a successful mocked response', () => {
    const fixture = TestBed.createComponent(RegisterPageComponent); fixture.detectChanges();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Este campo es obligatorio');
    http.expectNone(`${api}/auth/register`);

    const name = fixture.nativeElement.querySelector('#register-name') as HTMLInputElement;
    const email = fixture.nativeElement.querySelector('#register-email') as HTMLInputElement;
    const password = fixture.nativeElement.querySelector('#register-password') as HTMLInputElement;
    for (const [input, value] of [[name, 'QA test'], [email, 'qa@gameharbor.test'], [password, fixturePassword]] as const) { input.value = value; input.dispatchEvent(new Event('input', { bubbles: true })); }
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    http.expectOne(`${api}/auth/register`).flush(authResponse);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="status"]')?.textContent).toContain('Cuenta creada');
    fixture.destroy();
  });

  it('converts connection, forbidden and validation HTTP errors to messages', () => {
    expect(apiMessage({ status: 0 })).toContain('No se pudo conectar');
    expect(apiMessage({ status: 403, error: { error: 'Acceso denegado' } })).toContain('Acceso denegado');
    expect(apiMessage({ status: 400, error: { error: 'Datos invÃ¡lidos' } })).toContain('Datos invÃ¡lidos');
    expect(apiMessage({ status: 500, error: { error: 'Error interno del servidor' } })).toContain('Error interno del servidor');
  });
});

