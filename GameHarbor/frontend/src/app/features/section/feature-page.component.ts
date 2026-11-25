import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { finalize } from 'rxjs';
import { BibliotecaService, CarritoService, CloudService, MetodosPagoService, OrdenesService, RecomendacionesService } from '../../core/api/api.services';
import { BibliotecaItem, Carrito, MetodoPago, Orden, Recomendacion, SesionCloud } from '../../core/api/api.models';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../shared/components/loading/loading.component';
import { GameCardComponent } from '../../shared/components/game-card/game-card.component';
import { apiMessage } from '../catalog/catalog-page.component';
@Component({ selector: 'app-feature-page', imports: [RouterLink, DatePipe, DecimalPipe, EmptyStateComponent, LoadingComponent, GameCardComponent], templateUrl: './feature-page.component.html', styleUrl: './feature-page.component.css' })
export class FeaturePageComponent {
 private route=inject(ActivatedRoute); private router=inject(Router); private cartApi=inject(CarritoService); private orderApi=inject(OrdenesService); private methodsApi=inject(MetodosPagoService); private libraryApi=inject(BibliotecaService); private cloudApi=inject(CloudService); private recApi=inject(RecomendacionesService);
 protected section=signal(''); protected title=signal(''); protected heading=signal(''); protected description=signal(''); protected loading=signal(true); protected busy=signal(false); protected error=signal(''); protected message=signal(''); protected cart=signal<Carrito|null>(null); protected orders=signal<Orden[]>([]); protected library=signal<BibliotecaItem[]>([]); protected sessions=signal<SesionCloud[]>([]); protected recs=signal<Recomendacion[]>([]); protected methods=signal<MetodoPago[]>([]); protected paymentId=signal(0); protected limit=signal(10);
 constructor(){ this.route.url.subscribe(()=>{ const segment=this.router.url.split('?')[0].split('/')[1]||''; this.section.set(segment); this.title.set(segment==='cloud'?'GameHarbor Cloud':segment==='recomendaciones'?'Descubre más':'Tu cuenta'); this.heading.set(({carrito:'Carrito de compras',checkout:'Confirmar compra',ordenes:'Historial de órdenes',biblioteca:'Tu biblioteca',cloud:'Sesiones Cloud',recomendaciones:'Recomendaciones'} as Record<string,string>)[segment]||'GameHarbor'); this.description.set(segment==='cloud'?'Administra sesiones simuladas disponibles en tu cuenta.':'Información cargada de tu cuenta desde GameHarbor.'); this.load(); }); }
 protected load(){ this.loading.set(true); this.error.set(''); this.message.set(''); const done=()=>this.loading.set(false); const fail=(e:any)=>this.error.set(apiMessage(e)); switch(this.section()){
 case 'carrito': this.cartApi.obtener().pipe(finalize(done)).subscribe({next:x=>this.cart.set(x),error:fail}); break;
 case 'checkout': this.cartApi.obtener().pipe(finalize(done)).subscribe({next:x=>this.cart.set(x),error:fail}); this.methodsApi.listar().subscribe({next:x=>this.methods.set(x),error:fail}); break;
 case 'ordenes': this.orderApi.listar().pipe(finalize(done)).subscribe({next:x=>this.orders.set(x),error:fail}); break;
 case 'biblioteca': this.libraryApi.listar().pipe(finalize(done)).subscribe({next:x=>this.library.set(x),error:fail}); break;
 case 'cloud': this.cloudApi.activas().pipe(finalize(done)).subscribe({next:x=>this.sessions.set(x),error:fail}); break;
 case 'recomendaciones': this.recApi.listar(this.limit()).pipe(finalize(done)).subscribe({next:x=>this.recs.set(x),error:fail}); break;
 default: done(); }}
 protected remove(id:number){this.cartApi.quitar(id).subscribe({next:x=>this.cart.set(x),error:e=>this.error.set(apiMessage(e))});}
 protected clearCart(){if(!confirm('¿Vaciar todo el carrito?'))return;this.cartApi.vaciar().subscribe({next:x=>this.cart.set(x),error:e=>this.error.set(apiMessage(e))});}
 protected buy(){if(!this.paymentId()||this.busy()||!confirm('La operación es una compra simulada que generará una orden y añadirá los juegos a tu biblioteca. ¿Continuar?'))return;this.busy.set(true);this.orderApi.comprar(this.paymentId()).pipe(finalize(()=>this.busy.set(false))).subscribe({next:o=>{this.message.set(`Compra simulada completada. Orden #${o.id}.`);this.cart.set({items:[],total:0});},error:e=>this.error.set(apiMessage(e))});}
 protected startCloud(gameId:number){this.busy.set(true);this.cloudApi.iniciar(gameId).pipe(finalize(()=>this.busy.set(false))).subscribe({next:s=>{this.message.set(`Sesión simulada iniciada para ${s.juegoTitulo}.`);void this.router.navigate(['/cloud']);},error:e=>this.error.set(apiMessage(e))});}
 protected finishCloud(id:number){this.busy.set(true);this.cloudApi.finalizar(id).pipe(finalize(()=>this.busy.set(false))).subscribe({next:()=>{this.message.set('Sesión simulada finalizada.');this.cloudApi.activas().subscribe({next:s=>this.sessions.set(s),error:e=>this.error.set(apiMessage(e))});},error:e=>this.error.set(apiMessage(e))});}
}

