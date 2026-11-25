import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({ selector: 'app-header', imports: [RouterLink, NavbarComponent], templateUrl: './header.component.html', styleUrl: './header.component.css' })
export class HeaderComponent {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected logout(): void {
    this.auth.logout();
    void this.router.navigate(['/']);
  }
}
