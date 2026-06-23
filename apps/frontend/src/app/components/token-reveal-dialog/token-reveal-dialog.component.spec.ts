import { TestBed } from '@angular/core/testing';
import { TokenRevealDialogComponent } from './token-reveal-dialog.component';

describe('TokenRevealDialogComponent', () => {
  it('shows token and warning', () => {
    const fixture = TestBed.createComponent(TokenRevealDialogComponent);
    fixture.componentInstance.token = 'ff_live_secret';
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('ff_live_secret');
    expect(el.textContent).toContain('will not be shown again');
  });
});
