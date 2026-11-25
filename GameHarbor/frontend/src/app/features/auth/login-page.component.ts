import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LoadingComponent } from '../../shared/components/loading/loading.component';
import { LoginRequest } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink, LoadingComponent],
  templateUrl: './login-page.component.html',
  styleUrl: './auth-page.component.css',
})
export class LoginPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.maxLength(72)]],
  });
  protected readonly submitted = signal(false);
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal('');

  protected fieldError(name: 'email' | 'password'): string {
    const control = this.form.controls[name];
    if (!control.touched && !this.submitted()) return '';
    if (control.hasError('required')) return name === 'email' ? 'Ingresa tu correo electrónico.' : 'Ingresa tu contraseña.';
    if (control.hasError('email')) return 'Ingresa un correo electrónico válido.';
    if (control.hasError('maxlength')) return 'El valor supera la longitud permitida.';
    return '';
  }

  protected submit(): void {
    this.submitted.set(true);
    this.errorMessage.set('');
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.loading.set(true);
    const request: LoginRequest = { email: this.form.controls.email.value.trim(), password: this.form.controls.password.value };
    this.auth.login(request).subscribe({
      next: () => { this.loading.set(false); void this.router.navigateByUrl(this.safeReturnUrl()); },
      error: (error: unknown) => { this.loading.set(false); this.errorMessage.set(this.auth.errorMessage(error)); },
    });
  }

  private safeReturnUrl(): string {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    return returnUrl?.startsWith('/') && !returnUrl.startsWith('//') ? returnUrl : '/';
  }
}
