import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { BibliotecaItem, Carrito, Categoria, HistorialRequest, JuegoDetalle, JuegoResumen, MetodoPago, Orden, Pagina, Recomendacion, Resena, SesionCloud } from './api.models';

const API = `${environment.apiUrl}/api`;
@Injectable({ providedIn: 'root' }) export class JuegosService {
  private http = inject(HttpClient);
  listar(filtros: { q?: string; categoriaId?: number; pagina?: number; tamano?: number } = {}): Observable<Pagina<JuegoResumen>> {
    let params = new HttpParams().set('pagina', filtros.pagina ?? 0).set('tamano', filtros.tamano ?? 12);
    if (filtros.q) params = params.set('q', filtros.q);
    if (filtros.categoriaId) params = params.set('categoriaId', filtros.categoriaId);
    return this.http.get<Pagina<JuegoResumen>>(`${API}/juegos`, { params });
  }
  detalle(id: number) { return this.http.get<JuegoDetalle>(`${API}/juegos/${id}`); }
}
@Injectable({ providedIn: 'root' }) export class CategoriasService { private http = inject(HttpClient); listar() { return this.http.get<Categoria[]>(`${API}/categorias`); } }
@Injectable({ providedIn: 'root' }) export class ResenasService {
  private http = inject(HttpClient);
  listar(id: number) { return this.http.get<Resena[]>(`${API}/juegos/${id}/resenas`); }
  publicar(id: number, calificacion: number, comentario: string) { return this.http.post<Resena>(`${API}/juegos/${id}/resenas`, { calificacion, comentario }); }
}
@Injectable({ providedIn: 'root' }) export class CarritoService {
  private http = inject(HttpClient);
  obtener() { return this.http.get<Carrito>(`${API}/carrito`); }
  agregar(juegoId: number) { return this.http.post<Carrito>(`${API}/carrito/items`, { juegoId }); }
  quitar(juegoId: number) { return this.http.delete<Carrito>(`${API}/carrito/items/${juegoId}`); }
  vaciar() { return this.http.delete<Carrito>(`${API}/carrito`); }
}
@Injectable({ providedIn: 'root' }) export class MetodosPagoService { private http = inject(HttpClient); listar() { return this.http.get<MetodoPago[]>(`${API}/metodos-pago`); } }
@Injectable({ providedIn: 'root' }) export class OrdenesService {
  private http = inject(HttpClient);
  listar() { return this.http.get<Orden[]>(`${API}/ordenes`); }
  comprar(metodoPagoId: number) { return this.http.post<Orden>(`${API}/ordenes`, { metodoPagoId }); }
}
@Injectable({ providedIn: 'root' }) export class BibliotecaService { private http = inject(HttpClient); listar() { return this.http.get<BibliotecaItem[]>(`${API}/biblioteca`); } }
@Injectable({ providedIn: 'root' }) export class CloudService {
  private http = inject(HttpClient);
  activas() { return this.http.get<SesionCloud[]>(`${API}/cloud/sesiones/activas`); }
  iniciar(juegoId: number) { return this.http.post<SesionCloud>(`${API}/cloud/sesiones`, { juegoId }); }
  finalizar(id: number) { return this.http.post<SesionCloud>(`${API}/cloud/sesiones/${id}/finalizar`, {}); }
}
@Injectable({ providedIn: 'root' }) export class RecomendacionesService { private http = inject(HttpClient); listar(limite: number) { return this.http.get<Recomendacion[]>(`${API}/recomendaciones`, { params: { limite } }); } }
@Injectable({ providedIn: 'root' }) export class HistorialService { private http = inject(HttpClient); registrar(evento: HistorialRequest) { return this.http.post<void>(`${API}/historial`, evento); } }
