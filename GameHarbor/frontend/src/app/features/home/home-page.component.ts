import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { JuegosService } from '../../core/api/api.services';
import { JuegoResumen } from '../../core/api/api.models';
import { GameCardComponent } from '../../shared/components/game-card/game-card.component';
import { SectionHeaderComponent } from '../../shared/components/section-header/section-header.component';
import { LoadingComponent } from '../../shared/components/loading/loading.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
@Component({ selector: 'app-home-page', imports: [RouterLink, GameCardComponent, SectionHeaderComponent, LoadingComponent, EmptyStateComponent], templateUrl: './home-page.component.html', styleUrl: './home-page.component.css' })
export class HomePageComponent { private api=inject(JuegosService); protected games=signal<JuegoResumen[]>([]); protected loading=signal(true); protected error=signal(''); constructor(){this.api.listar({tamano:3}).pipe(finalize(()=>this.loading.set(false))).subscribe({next:p=>this.games.set(p.contenido),error:()=>this.error.set('No se pudo cargar el catálogo. Comprueba que GameHarbor esté disponible.')});} }

