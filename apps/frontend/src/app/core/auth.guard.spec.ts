import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  it('redirects to login without token', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { getToken: () => null } },
      ],
    });
    const router = TestBed.inject(Router);
    spyOn(router, 'createUrlTree').and.callThrough();
    TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
  });
});
