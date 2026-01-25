import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AccountService } from './account.service';

describe('AccountService', () => {
  let service: AccountService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(AccountService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('findByLogin uses q param with trimmed value', () => {
    service.findByLogin('  admin  ').subscribe();

    const req = httpMock.expectOne((r: any) => r.url === '/api/accounts');
    expect(req.request.params.get('q')).toBe('admin');
    req.flush({ id: 1 });
  });
});







