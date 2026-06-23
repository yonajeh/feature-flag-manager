import { TestBed } from '@angular/core/testing';
import { HttpRequest, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { getToken: () => 'jwt-token' } },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('attaches bearer token to admin API calls', () => {
  const req = new HttpRequest('GET', '/api/admin/applications');
  TestBed.runInInjectionContext(() =>
    authInterceptor(req, (r) => {
      expect(r.headers.get('Authorization')).toBe('Bearer jwt-token');
      return { subscribe: () => undefined } as never;
    })
  );
  });
});
