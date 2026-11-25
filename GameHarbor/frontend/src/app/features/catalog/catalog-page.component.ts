import { Component, inject, signal } from '@angular/core';
import { GameCardComponent } from '../../shared/components/game-card/game-card.component';
import { SearchBarComponent } from '../../shared/components/search-bar/search-bar.component';
import { SectionHeaderComponent } from '../../shared/components/section-header/section-header.component';
import { LoadingComponent } from '../../shared/components/loading/loading.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { CategoriasService, JuegosService } from '../../core/api/api.services';
import { Categoria, JuegoResumen } from '../../core/api/api.models';
import { SearchBarComponent as SearchInput } from '../../shared/components/search-bar/search-bar.component';
import { finalize } from 'rxjs';
@Component({ selector: 'app-catalog-page', imports: [GameCardComponent, SearchBarComponent, SectionHeaderComponent, LoadingComponent, EmptyStateComponent], templateUrl: './catalog-page.component.html', styleUrl: './catalog-page.component.css' })
export class CatalogPageComponent {
  private readonly juegosApi = inject(JuegosService); private readonly categoriasApi = inject(CategoriasService);
  protected readonly games = signal<JuegoResumen[]>([]); protected readonly categories = signal<Categoria[]>([]); protected readonly categoryError = signal(''); protected readonly loading = signal(true); protected readonly error = signal('');
  protected readonly pageNumber = signal(0); protected readonly pageCount = signal(0); protected readonly total = signal(0); protected readonly categoryId = signal<number | null>(null); private query = '';
  constructor() { this.categoriasApi.listar().subscribe({ next: x => this.categories.set(x), error: e => this.categoryError.set(apiMessage(e)) }); this.load(); }
  protected load(): void { this.loading.set(true); this.error.set(''); this.juegosApi.listar({ q: this.query || undefined, categoriaId: this.categoryId() ?? undefined, pagina: this.pageNumber() }).pipe(finalize(() => this.loading.set(false))).subscribe({ next: p => { this.games.set(p.contenido); this.pageCount.set(p.totalPaginas); this.total.set(p.totalElementos); }, error: e => this.error.set(apiMessage(e)) }); }
  protected search(value: string): void { this.query = value; this.pageNumber.set(0); this.load(); }
  protected selectCategory(event: Event): void { const value = (event.target as HTMLSelectElement).value; this.categoryId.set(value ? Number(value) : null); this.pageNumber.set(0); this.load(); }
  protected page(delta: number): void { this.pageNumber.update(p => p + delta); this.load(); }
  protected resetFilters(input: SearchInput): void { input.query = ''; this.query = ''; this.categoryId.set(null); this.pageNumber.set(0); this.load(); }
}
export function apiMessage(error: any): string { return error?.status === 0 ? 'No se pudo conectar con GameHarbor. Comprueba que el backend esté disponible.' : (error?.error?.error || error?.error?.message || `Error de comunicación (${error?.status ?? 'desconocido'}).`); }


