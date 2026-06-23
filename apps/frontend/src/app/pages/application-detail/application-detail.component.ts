import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { downloadJson } from '../../core/download-json';
import { ApiTokenMetadata, FeatureFlag } from '../../models';
import { TokenRevealDialogComponent } from '../../components/token-reveal-dialog/token-reveal-dialog.component';
import { slugFromText } from '../../core/slug-from-text';

@Component({
  selector: 'app-application-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, TokenRevealDialogComponent],
  template: `
    <div class="max-w-5xl mx-auto p-6 space-y-6" *ngIf="applicationId">
      <header class="flex items-center justify-between">
        <div>
          <a routerLink="/applications" class="text-sm text-indigo-600 hover:underline">← Applications</a>
          <h1 class="text-2xl font-semibold mt-1">Application</h1>
        </div>
        <div class="flex items-center gap-3">
          <button class="text-sm text-indigo-600 hover:text-indigo-800" (click)="exportData()">Export JSON</button>
          <button class="text-sm text-slate-600" (click)="auth.logout()">Logout</button>
        </div>
      </header>

      <nav class="border-b border-slate-200" aria-label="Application sections">
        <div class="flex gap-6">
          <a
            [routerLink]="['/applications', applicationId, 'tokens']"
            routerLinkActive="border-indigo-600 text-indigo-600"
            [routerLinkActiveOptions]="{ exact: true }"
            class="pb-3 text-sm font-medium border-b-2 -mb-px border-transparent text-slate-500 hover:text-slate-700 transition-colors"
          >
            Tokens
          </a>
          <a
            [routerLink]="['/applications', applicationId, 'features']"
            routerLinkActive="border-indigo-600 text-indigo-600"
            [routerLinkActiveOptions]="{ exact: true }"
            class="pb-3 text-sm font-medium border-b-2 -mb-px border-transparent text-slate-500 hover:text-slate-700 transition-colors"
          >
            Features
          </a>
        </div>
      </nav>

      <section *ngIf="activeTab === 'features'" class="bg-white rounded-lg shadow p-4 space-y-3">
        <h2 class="font-medium">Feature flags</h2>
        <div class="grid md:grid-cols-4 gap-2">
          <input
            class="border rounded px-3 py-2"
            placeholder="Description"
            [ngModel]="flagDescription"
            (ngModelChange)="onFlagDescriptionChange($event)"
          />
          <input
            class="border rounded px-3 py-2"
            placeholder="flag-key"
            [ngModel]="flagKey"
            (ngModelChange)="onFlagKeyChange($event)"
          />
          <input
            class="border rounded px-3 py-2"
            placeholder='Metadata JSON e.g. {"owner":"team-a"}'
            [(ngModel)]="flagMetadata"
          />
          <button class="bg-indigo-600 text-white rounded px-4 py-2" (click)="addFlag()">Add flag</button>
        </div>
        <p *ngIf="metadataError" class="text-sm text-red-600">{{ metadataError }}</p>
        <table class="w-full text-sm mt-2">
          <thead class="bg-slate-100 text-left">
            <tr>
              <th class="p-2">Key</th>
              <th class="p-2">Enabled</th>
              <th class="p-2">Description</th>
              <th class="p-2">Metadata</th>
              <th class="p-2"></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let flag of flags" class="border-t">
              <td class="p-2 font-mono">{{ flag.key }}</td>
              <td class="p-2">
                <button
                  class="px-2 py-1 rounded text-xs"
                  [class.bg-green-100]="flag.enabled"
                  [class.bg-slate-200]="!flag.enabled"
                  (click)="toggle(flag)"
                >
                  {{ flag.enabled ? 'Ready' : 'Flagged' }}
                </button>
              </td>
              <td class="p-2">{{ flag.description || '—' }}</td>
              <td class="p-2 font-mono text-xs max-w-xs truncate" [title]="formatMetadata(flag.metadata)">
                {{ formatMetadata(flag.metadata) }}
              </td>
              <td class="p-2 text-right space-x-2">
                <button class="text-indigo-600 hover:underline" (click)="editMetadata(flag)">Edit metadata</button>
                <button class="text-red-600 hover:underline" (click)="deleteFlag(flag)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <section *ngIf="activeTab === 'tokens'" class="bg-white rounded-lg shadow p-4 space-y-3">
        <div class="flex items-center justify-between">
          <h2 class="font-medium">API tokens</h2>
          <button class="bg-indigo-600 text-white rounded px-4 py-2 text-sm" (click)="generateToken()">
            Generate token
          </button>
        </div>
        <table class="w-full text-sm">
          <thead class="bg-slate-100 text-left">
            <tr>
              <th class="p-2">Prefix</th>
              <th class="p-2">Status</th>
              <th class="p-2">Last used</th>
              <th class="p-2"></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let token of tokens" class="border-t">
              <td class="p-2 font-mono">{{ token.tokenPrefix }}…</td>
              <td class="p-2">{{ token.status }}</td>
              <td class="p-2">{{ token.lastUsedAt || '—' }}</td>
              <td class="p-2 text-right space-x-2">
                <button
                  *ngIf="token.status === 'ACTIVE'"
                  class="text-indigo-600 hover:underline"
                  (click)="rotate(token)"
                >
                  Rotate
                </button>
                <button
                  *ngIf="token.status === 'ACTIVE'"
                  class="text-red-600 hover:underline"
                  (click)="revoke(token)"
                >
                  Revoke
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>

    <app-token-reveal-dialog *ngIf="revealedToken" [token]="revealedToken" (closed)="revealedToken = null" />
  `,
})
export class ApplicationDetailComponent implements OnInit {
  applicationId = '';
  applicationName = '';
  activeTab: 'features' | 'tokens' = 'features';
  flags: FeatureFlag[] = [];
  tokens: ApiTokenMetadata[] = [];
  flagKey = '';
  flagDescription = '';
  flagKeyManuallyEdited = false;
  flagMetadata = '';
  metadataError = '';
  revealedToken: string | null = null;

  constructor(
    public auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService
  ) {}

  ngOnInit(): void {
    this.applicationId = this.route.snapshot.paramMap.get('id') ?? '';
    this.route.paramMap.subscribe((params) => {
      const tab = params.get('tab');
      if (tab === 'tokens') {
        this.activeTab = 'tokens';
      } else if (tab === 'features') {
        this.activeTab = 'features';
      } else {
        void this.router.navigate(['/applications', this.applicationId, 'features'], { replaceUrl: true });
      }
    });
    this.refresh();
  }

  refresh(): void {
    this.api.listFlags(this.applicationId).subscribe((flags) => (this.flags = flags));
    this.api.listTokens(this.applicationId).subscribe((tokens) => (this.tokens = tokens));
    this.api.listApplications().subscribe((apps) => {
      const app = apps.find((candidate) => candidate.id === this.applicationId);
      this.applicationName = app?.name ?? this.applicationId;
    });
  }

  exportData(): void {
    this.api.exportApplication(this.applicationId).subscribe((data) => {
      downloadJson(`${this.applicationName}-export.json`, data);
    });
  }

  onFlagDescriptionChange(value: string): void {
    this.flagDescription = value;
    if (!this.flagKeyManuallyEdited) {
      this.flagKey = slugFromText(value);
    }
  }

  onFlagKeyChange(value: string): void {
    this.flagKey = value;
    this.flagKeyManuallyEdited = true;
  }

  addFlag(): void {
    this.metadataError = '';
    const metadata = this.parseMetadataInput(this.flagMetadata);
    if (this.flagMetadata.trim() && metadata === undefined) {
      return;
    }

    this.api
      .createFlag(this.applicationId, this.flagKey, true, this.flagDescription || undefined, metadata)
      .subscribe({
        next: () => {
          this.flagKey = '';
          this.flagDescription = '';
          this.flagKeyManuallyEdited = false;
          this.flagMetadata = '';
          this.refresh();
        },
      });
  }

  toggle(flag: FeatureFlag): void {
    this.api
      .updateFlag(this.applicationId, flag.key, {
        enabled: !flag.enabled,
        description: flag.description,
        metadata: flag.metadata,
      })
      .subscribe(() => this.refresh());
  }

  formatMetadata(metadata?: Record<string, unknown>): string {
    if (!metadata || Object.keys(metadata).length === 0) {
      return '—';
    }
    return JSON.stringify(metadata);
  }

  editMetadata(flag: FeatureFlag): void {
    const current = flag.metadata ? JSON.stringify(flag.metadata, null, 2) : '';
    const input = window.prompt('Metadata JSON', current);
    if (input === null) {
      return;
    }

    this.metadataError = '';
    const metadata = this.parseMetadataInput(input);
    if (input.trim() && metadata === undefined) {
      return;
    }

    this.api
      .updateFlag(this.applicationId, flag.key, {
        enabled: flag.enabled,
        description: flag.description,
        metadata,
      })
      .subscribe(() => this.refresh());
  }

  private parseMetadataInput(input: string): Record<string, unknown> | undefined {
    const trimmed = input.trim();
    if (!trimmed) {
      return undefined;
    }

    try {
      const parsed = JSON.parse(trimmed) as unknown;
      if (parsed === null || Array.isArray(parsed) || typeof parsed !== 'object') {
        this.metadataError = 'Metadata must be a JSON object';
        return undefined;
      }
      return parsed as Record<string, unknown>;
    } catch {
      this.metadataError = 'Metadata must be valid JSON';
      return undefined;
    }
  }

  deleteFlag(flag: FeatureFlag): void {
    this.api.deleteFlag(this.applicationId, flag.key).subscribe(() => this.refresh());
  }

  generateToken(): void {
    this.api.generateToken(this.applicationId).subscribe((created) => {
      this.revealedToken = created.plaintextToken;
      this.refresh();
    });
  }

  rotate(token: ApiTokenMetadata): void {
    this.api.rotateToken(this.applicationId, token.id).subscribe((created) => {
      this.revealedToken = created.plaintextToken;
      this.refresh();
    });
  }

  revoke(token: ApiTokenMetadata): void {
    this.api.revokeToken(this.applicationId, token.id).subscribe(() => this.refresh());
  }
}
