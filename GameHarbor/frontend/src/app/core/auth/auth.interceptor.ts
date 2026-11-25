import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

function isProtectedGameHarborRequest(url: string, method: string): boolean {
  let requestUrl: URL;
  let apiUrl: URL;
  try {
    apiUrl = new URL(environment.apiUrl || window.location.origin);
    requestUrl = new URL(url, apiUrl);
  } catch {
    return false;
  }
  if (requestUrl.origin !== apiUrl.origin || !requestUrl.pathname.startsWith('/api/')) return false;

  const path = requestUrl.pathname;
  return /^\/api\/(carrito|ordenes|biblioteca|recomendaciones|historial)(\/|$)/.test(path)
    || /^\/api\/cloud\/sesiones(\/|$)/.test(path)
    || (method === 'POST' && /^\/api\/juegos\/\d+\/resenas$/.test(path));
}

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);

  if (!isProtectedGameHarborRequest(request.url, request.method)) return next(request);

  const token = auth.getToken();
  const authenticatedRequest = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401) auth.handleUnauthorized();
      if (error instanceof HttpErrorResponse && error.status === 403) {
        // A 403 means the server authenticated the request but denied this operation; keep the session.
      }
      return throwError(() => error);
    }),
  );
};


