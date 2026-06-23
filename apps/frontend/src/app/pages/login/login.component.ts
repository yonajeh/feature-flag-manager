import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center p-4">
      <form class="w-full max-w-md bg-white shadow rounded-lg p-8 space-y-4" (ngSubmit)="submit()">
        <h1 class="text-2xl font-semibold">Feature Flag Manager</h1>
        <p class="text-sm text-slate-600">Super admin sign in</p>
        <div>
          <label class="block text-sm font-medium mb-1">Username</label>
          <input class="w-full border rounded px-3 py-2" [(ngModel)]="username" name="username" required />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">Password</label>
          <input
            class="w-full border rounded px-3 py-2"
            type="password"
            [(ngModel)]="password"
            name="password"
            required
          />
        </div>
        <p *ngIf="error" class="text-sm text-red-600">{{ error }}</p>
        <button class="w-full bg-indigo-600 text-white rounded py-2 hover:bg-indigo-700" type="submit">
          Sign in
        </button>
      </form>
    </div>
  `,
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    this.error = '';
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/applications']),
      error: () => (this.error = 'Invalid credentials'),
    });
  }
}
