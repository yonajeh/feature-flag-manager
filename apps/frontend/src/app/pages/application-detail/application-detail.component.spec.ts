import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ApplicationDetailComponent } from './application-detail.component';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

describe('ApplicationDetailComponent', () => {
  const api = jasmine.createSpyObj<ApiService>('ApiService', [
    'listFlags',
    'listTokens',
    'updateFlag',
    'createFlag',
    'deleteFlag',
  ]);

  beforeEach(() => {
    api.listFlags.and.returnValue(
      of([
        {
          id: '1',
          applicationId: 'app',
          key: 'instant-booking',
          enabled: true,
          createdAt: '',
          updatedAt: '',
        },
      ])
    );
    api.listTokens.and.returnValue(of([]));
    api.updateFlag.and.returnValue(
      of({
        id: '1',
        applicationId: 'app',
        key: 'instant-booking',
        enabled: false,
        createdAt: '',
        updatedAt: '',
      })
    );
    api.listApplications = jasmine.createSpy('listApplications').and.returnValue(of([]));

    TestBed.configureTestingModule({
      imports: [ApplicationDetailComponent],
      providers: [
        provideRouter([]),
        { provide: ApiService, useValue: api },
        { provide: AuthService, useValue: { logout: () => undefined } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: 'app', tab: 'features' }),
            },
            paramMap: of(convertToParamMap({ id: 'app', tab: 'features' })),
          },
        },
      ],
    });
  });

  it('renders flags and toggles value', () => {
    const fixture = TestBed.createComponent(ApplicationDetailComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('instant-booking');
    fixture.componentInstance.toggle(fixture.componentInstance.flags[0]);
    expect(api.updateFlag).toHaveBeenCalledWith('app', 'instant-booking', {
      enabled: false,
      description: undefined,
      metadata: undefined,
    });
  });
});
