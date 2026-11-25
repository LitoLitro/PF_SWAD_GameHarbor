import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { JuegosService, ResenasService, CarritoService, HistorialService } from '../../core/api/api.services';
import { JuegoDetalle, Resena } from '../../core/api/api.models';
import { AuthService } from '../../core/auth/auth.service';
import { LoadingComponent } from '../../shared/components/loading/loading.component';
import { apiMessage } from '../catalog/catalog-page.component';
@Component({ selector: 'app-game-detail-page', imports: [RouterLink, DecimalPipe, DatePipe, LoadingComponent], templateUrl: './game-detail-page.component.html', styleUrl: './game-detail-page.component.css' })
export class GameDetailPageComponent {
 private route=inject(ActivatedRoute); private juegos=inject(JuegosService); private resenasApi=inject(ResenasService); private carrito=inject(CarritoService); private historial=inject(HistorialService); private auth=inject(AuthService);
 protected game=signal<JuegoDetalle|null>(null); protected reviews=signal<Resena[]>([]); protected loading=signal(true); protected error=signal(''); protected reviewError=signal(''); protected reviewMessage=signal(''); protected message=signal(''); protected rating=signal(5); protected comment=signal(''); protected reviewBusy=signal(false); protected actionBusy=signal(false);
 constructor(){this.route.paramMap.subscribe(params=>{const id=Number(params.get('id'));this.loading.set(true);forkJoin({game:this.juegos.detalle(id),reviews:this.resenasApi.listar(id)}).pipe(finalize(()=>this.loading.set(false))).subscribe({next:r=>{this.game.set(r.game);this.reviews.set(r.reviews);this.error.set('');if(this.auth.isAuthenticated())this.historial.registrar({juegoId:id,segundos:0,origen:'CATALOGO'}).subscribe({error:()=>{}});},error:e=>this.error.set(apiMessage(e))});});}
 protected addToCart(){const id=this.game()?.id;if(!id)return;this.actionBusy.set(true);this.message.set('');this.carrito.agregar(id).pipe(finalize(()=>this.actionBusy.set(false))).subscribe({next:()=>this.message.set('Juego añadido al carrito.'),error:e=>this.message.set(apiMessage(e))});}
 protected submitReview(){const id=this.game()?.id;if(!id)return;this.reviewBusy.set(true);this.reviewError.set('');this.reviewMessage.set('');this.resenasApi.publicar(id,this.rating(),this.comment().trim()).pipe(finalize(()=>this.reviewBusy.set(false))).subscribe({next:()=>{this.comment.set('');this.reviewMessage.set('Reseña publicada correctamente.');this.resenasApi.listar(id).subscribe({next:r=>this.reviews.set(r),error:e=>this.reviewError.set(apiMessage(e))});},error:e=>this.reviewError.set(apiMessage(e))});}
}


