import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../environments/environment';
import { BibliotecaService, CarritoService, CategoriasService, CloudService, HistorialService, JuegosService, MetodosPagoService, OrdenesService, RecomendacionesService, ResenasService } from './api.services';

const api = `${environment.apiUrl}/api`;
describe('GameHarbor API services', () => {
  let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] }); http = TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());

  it('sends catalog search, category filter and paging as server parameters', () => {
    TestBed.inject(JuegosService).listar({ q: 'nimbus', categoriaId: 2, pagina: 1, tamano: 5 }).subscribe();
    const req = http.expectOne(r => r.url === `${api}/juegos`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('q')).toBe('nimbus');
    expect(req.request.params.get('categoriaId')).toBe('2');
    expect(req.request.params.get('pagina')).toBe('1');
    expect(req.request.params.get('tamano')).toBe('5');
    req.flush({ contenido: [], pagina: 1, tamano: 5, totalElementos: 0, totalPaginas: 0 });
    TestBed.inject(JuegosService).detalle(9).subscribe();
    http.expectOne(`${api}/juegos/9`).flush({});
  });

  it('uses category, review and payment method endpoints and real request fields', () => {
    TestBed.inject(CategoriasService).listar().subscribe(); http.expectOne(`${api}/categorias`).flush([]);
    TestBed.inject(MetodosPagoService).listar().subscribe(); http.expectOne(`${api}/metodos-pago`).flush([]);
    const reviews = TestBed.inject(ResenasService);
    reviews.listar(4).subscribe(); http.expectOne(`${api}/juegos/4/resenas`).flush([]);
    reviews.publicar(4, 5, 'Prueba mock').subscribe();
    const reviewRequest = http.expectOne(`${api}/juegos/4/resenas`);
    expect(reviewRequest.request.method).toBe('POST');
    expect(reviewRequest.request.body).toEqual({ calificacion: 5, comentario: 'Prueba mock' });
    reviewRequest.flush({ id: 1, usuario: 'QA', calificacion: 5, comentario: 'Prueba mock', fecha: '2026-01-01T10:00:00' });
  });

  it('uses the authenticated user cart endpoints and juegoId body', () => {
    const cart = TestBed.inject(CarritoService);
    cart.obtener().subscribe(); http.expectOne(`${api}/carrito`).flush({ items: [], total: 0 });
    cart.agregar(4).subscribe();
    const add = http.expectOne(`${api}/carrito/items`);
    expect(add.request.method).toBe('POST'); expect(add.request.body).toEqual({ juegoId: 4 });
    add.flush({ items: [], total: 0 });
    cart.quitar(4).subscribe(); http.expectOne(`${api}/carrito/items/4`).flush({ items: [], total: 0 });
    cart.vaciar().subscribe(); http.expectOne(r => r.url === `${api}/carrito` && r.method === 'DELETE').flush({ items: [], total: 0 });
  });

  it('uses order and library endpoints with metodoPagoId', () => {
    const orders = TestBed.inject(OrdenesService);
    orders.comprar(3).subscribe();
    const order = http.expectOne(`${api}/ordenes`);
    expect(order.request.method).toBe('POST'); expect(order.request.body).toEqual({ metodoPagoId: 3 });
    order.flush({ id: 1, fecha: '2026-01-01T10:00:00', total: 1, estado: 'PAGADA', metodoPago: 'QA', detalles: [] });
    orders.listar().subscribe(); http.expectOne(`${api}/ordenes`).flush([]);
    TestBed.inject(BibliotecaService).listar().subscribe(); http.expectOne(`${api}/biblioteca`).flush([]);
  });

  it('uses cloud, history and stored recommendations contracts', () => {
    const cloud = TestBed.inject(CloudService);
    cloud.activas().subscribe(); http.expectOne(`${api}/cloud/sesiones/activas`).flush([]);
    cloud.iniciar(6).subscribe();
    const start = http.expectOne(`${api}/cloud/sesiones`);
    expect(start.request.body).toEqual({ juegoId: 6 }); start.flush({});
    cloud.finalizar(5).subscribe();
    const finish = http.expectOne(`${api}/cloud/sesiones/5/finalizar`);
    expect(finish.request.body).toEqual({}); finish.flush({});
    TestBed.inject(RecomendacionesService).listar(7).subscribe();
    const recommendations = http.expectOne(`${api}/recomendaciones?limite=7`); recommendations.flush([]);
    TestBed.inject(HistorialService).registrar({ juegoId: 6, segundos: 10, origen: 'CATALOGO' }).subscribe();
    const history = http.expectOne(`${api}/historial`);
    expect(history.request.method).toBe('POST'); expect(history.request.body).toEqual({ juegoId: 6, segundos: 10, origen: 'CATALOGO' }); history.flush(null);
  });
});
