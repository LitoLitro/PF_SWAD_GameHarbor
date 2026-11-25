import { Component, inject, OnDestroy, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { LoadingComponent } from '../../shared/components/loading/loading.component';
import { AuthService } from '../../core/auth/auth.service';
import { RegisterRequest } from '../../core/auth/auth.models';

@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink, LoadingComponent],
  templateUrl: './register-page.component.html',
  styleUrl: './auth-page.component.css',
})
export class RegisterPageComponent implements OnDestroy {
  private readonly formBuilder = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private redirectTimer: ReturnType<typeof setTimeout> | undefined;

  protected readonly form = this.formBuilder.nonNullable.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });
  protected readonly submitted = signal(false);
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');

  ngOnDestroy(): void { if (this.redirectTimer) clearTimeout(this.redirectTimer); }

  protected fieldError(name: 'nombre' | 'email' | 'password'): string {
    const control = this.form.controls[name];
    if (!control.touched && !this.submitted()) return '';
    if (control.hasError('required')) return 'Este campo es obligatorio.';
    if (control.hasError('pattern')) return 'Ingresa al menos un carácter que no sea espacio.';
    if (control.hasError('email')) return 'Ingresa un correo electrónico válido.';
    if (control.hasError('minlength')) return 'La contraseña debe tener al menos 8 caracteres.';
    if (control.hasError('maxlength')) return name === 'nombre' ? 'El nombre admite hasta 80 caracteres.' : name === 'email' ? 'El correo admite hasta 150 caracteres.' : 'La contraseña admite hasta 72 caracteres.';
    return '';
  }

  protected submit(): void {
    this.submitted.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.loading.set(true);
    const request: RegisterRequest = {
      nombre: this.form.controls.nombre.value.trim(),
      email: this.form.controls.email.value.trim(),
      password: this.form.controls.password.value,
    };
    this.auth.register(request).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set('Cuenta creada. Tu sesión está activa y te llevaremos al inicio.');
        this.redirectTimer = setTimeout(() => void this.router.navigateByUrl('/'), 1100);
      },
      error: (error: unknown) => { this.loading.set(false); this.errorMessage.set(this.auth.errorMessage(error)); },
    });
  }
}
