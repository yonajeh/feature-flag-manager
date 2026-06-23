import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { downloadJson } from '../../core/download-json';
import { Application } from '../../models';

@Component({
  selector: 'app-applications',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="max-w-5xl mx-auto p-6 space-y-6">
      <header class="flex items-center justify-between">
        <h1 class="text-2xl font-semibold">Applications</h1>
        <div class="flex items-center gap-3">
          <button
            class="text-sm text-indigo-600 hover:text-indigo-800"
            (click)="exportAll()"
            [disabled]="exporting"
          >
            {{ exporting ? 'Exporting…' : 'Export all' }}
          </button>
          <button class="text-sm text-slate-600 hover:text-slate-900" (click)="auth.logout()">Logout</button>
        </div>
      </header>

      <section class="bg-white rounded-lg shadow p-4 space-y-3">
        <h2 class="font-medium">Register application</h2>
        <div class="grid md:grid-cols-3 gap-3">
          <input class="border rounded px-3 py-2" placeholder="slug-name" [(ngModel)]="newName" />
          <input class="border rounded px-3 py-2" placeholder="Display name" [(ngModel)]="newDisplayName" />
          <button class="bg-indigo-600 text-white rounded px-4 py-2" (click)="create()">Create</button>
        </div>
        <p *ngIf="error" class="text-sm text-red-600">{{ error }}</p>
      </section>

      <section class="bg-white rounded-lg shadow overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-slate-100 text-left">
            <tr>
              <th class="p-3">Name</th>
              <th class="p-3">Display</th>
              <th class="p-3"></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let app of applications" class="border-t">
              <td class="p-3 font-mono">{{ app.name }}</td>
              <td class="p-3">{{ app.displayName }}</td>
              <td class="p-3 text-right space-x-2">
                <a class="text-indigo-600 hover:underline" [routerLink]="['/applications', app.id]">Manage</a>
                <button class="text-indigo-600 hover:underline" (click)="exportApp(app)">Export</button>
                <button class="text-red-600 hover:underline" (click)="remove(app)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  `,
})
export class ApplicationsComponent implements OnInit {
  applications: Application[] = [];
  newName = '';
  newDisplayName = '';
  error = '';
  exporting = false;

  constructor(public auth: AuthService, private api: ApiService, private router: Router) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.listApplications().subscribe((apps) => (this.applications = apps));
  }

  create(): void {
    this.error = '';
    this.api.createApplication(this.newName, this.newDisplayName).subscribe({
      next: () => {
        this.newName = '';
        this.newDisplayName = '';
        this.load();
      },
      error: () => (this.error = 'Could not create application (name may already exist)'),
    });
  }

  remove(app: Application): void {
    if (!confirm(`Delete ${app.name} and all flags/tokens?`)) {
      return;
    }
    this.api.deleteApplication(app.id).subscribe(() => this.load());
  }

  exportApp(app: Application): void {
    this.api.exportApplication(app.id).subscribe((data) => {
      downloadJson(`${app.name}-export.json`, data);
    });
  }

  exportAll(): void {
    this.exporting = true;
    this.api.exportAll().subscribe({
      next: (data) => {
        downloadJson('ffm-export.json', data);
        this.exporting = false;
      },
      error: () => {
        this.exporting = false;
      },
    });
  }
}
