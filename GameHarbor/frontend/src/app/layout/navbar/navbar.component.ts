import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

interface NavigationItem { label: string; path: string; }

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected menuOpen = false;
  private readonly publicItems: NavigationItem[] = [
    { label: 'Inicio', path: '/' },
    { label: 'Catálogo', path: '/catalogo' },
  ];
  private readonly privateItems: NavigationItem[] = [
    { label: 'Carrito', path: '/carrito' },
    { label: 'Órdenes', path: '/ordenes' },
    { label: 'Biblioteca', path: '/biblioteca' },
    { label: 'Cloud Play', path: '/cloud' },
    { label: 'Para ti', path: '/recomendaciones' },
  ];

  protected get items(): NavigationItem[] {
    return this.auth.isAuthenticated() ? [...this.publicItems, ...this.privateItems] : this.publicItems;
  }

  protected closeMenu(): void { this.menuOpen = false; }
  protected toggleMenu(): void { this.menuOpen = !this.menuOpen; }
  protected logout(): void {
    this.auth.logout();
    this.closeMenu();
    void this.router.navigate(['/']);
  }
}
