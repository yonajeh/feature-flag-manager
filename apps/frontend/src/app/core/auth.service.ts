import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { LoginResponse } from '../models';

const TOKEN_KEY = 'ff_admin_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly isLoggedIn = signal(!!localStorage.getItem(TOKEN_KEY));

  constructor(private http: HttpClient, private router: Router) {}

  login(username: string, password: string) {
    return this.http
      .post<LoginResponse>('/api/admin/auth/login', { username, password })
      .pipe(
        tap((response) => {
          localStorage.setItem(TOKEN_KEY, response.token);
          this.isLoggedIn.set(true);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.isLoggedIn.set(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }
}
