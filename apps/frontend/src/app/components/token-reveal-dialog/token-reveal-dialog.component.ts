import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-token-reveal-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
      <div class="bg-white rounded-lg shadow-lg max-w-lg w-full p-6 space-y-4">
        <h2 class="text-lg font-semibold">API token created</h2>
        <p class="text-sm text-amber-700 bg-amber-50 border border-amber-200 rounded p-3">
          Copy this token now. It will not be shown again.
        </p>
        <code class="block bg-slate-100 p-3 rounded text-xs break-all">{{ token }}</code>
        <div class="flex justify-end gap-2">
          <button class="border rounded px-4 py-2" (click)="copy()">Copy</button>
          <button class="bg-indigo-600 text-white rounded px-4 py-2" (click)="closed.emit()">Done</button>
        </div>
        <p *ngIf="copied" class="text-sm text-green-700">Copied to clipboard</p>
      </div>
    </div>
  `,
})
export class TokenRevealDialogComponent {
  @Input() token = '';
  @Output() closed = new EventEmitter<void>();
  copied = false;

  async copy(): Promise<void> {
    await navigator.clipboard.writeText(this.token);
    this.copied = true;
  }
}
