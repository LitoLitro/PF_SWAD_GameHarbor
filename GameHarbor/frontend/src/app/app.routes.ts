import { Routes } from '@angular/router';
import { LoginPageComponent } from './features/auth/login-page.component';
import { RegisterPageComponent } from './features/auth/register-page.component';
import { CatalogPageComponent } from './features/catalog/catalog-page.component';
import { GameDetailPageComponent } from './features/game-detail/game-detail-page.component';
import { HomePageComponent } from './features/home/home-page.component';
import { FeaturePageComponent } from './features/section/feature-page.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { authGuard } from './core/auth/auth.guard';
export const routes: Routes = [{ path: '', component: MainLayoutComponent, children: [
 { path: '', component: HomePageComponent, title: 'Inicio · GameHarbor' },
 { path: 'login', component: LoginPageComponent, title: 'Iniciar sesión · GameHarbor' },
 { path: 'registro', component: RegisterPageComponent, title: 'Crear cuenta · GameHarbor' },
 { path: 'catalogo', component: CatalogPageComponent, title: 'Catálogo · GameHarbor' },
 { path: 'catalogo/:id', component: GameDetailPageComponent, title: 'Detalle del juego · GameHarbor' },
 { path: 'carrito', component: FeaturePageComponent, canActivate: [authGuard], title: 'Carrito · GameHarbor' },
 { path: 'checkout', component: FeaturePageComponent, canActivate: [authGuard], title: 'Confirmar compra · GameHarbor' },
 { path: 'ordenes', component: FeaturePageComponent, canActivate: [authGuard], title: 'Historial de órdenes · GameHarbor' },
 { path: 'historial', redirectTo: 'ordenes', pathMatch: 'full' },
 { path: 'biblioteca', component: FeaturePageComponent, canActivate: [authGuard], title: 'Biblioteca · GameHarbor' },
 { path: 'cloud', component: FeaturePageComponent, canActivate: [authGuard], title: 'Cloud Gaming · GameHarbor' },
 { path: 'recomendaciones', component: FeaturePageComponent, canActivate: [authGuard], title: 'Recomendaciones · GameHarbor' },
 { path: '**', redirectTo: '' },
]}];
