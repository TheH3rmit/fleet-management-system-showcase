import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CargoService } from './cargo.service';

describe('CargoService', () => {
  let service: CargoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(CargoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('list should append sort params', () => {
    service.list({ q: 'foo', sort: ['status,desc', 'id,asc'], page: 0, size: 10 }).subscribe();

    const req = httpMock.expectOne((r: any) => r.url === '/api/cargos');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.getAll('sort')).toEqual(['status,desc', 'id,asc']);
    expect(req.request.params.get('q')).toBe('foo');
    req.flush({ content: [], totalElements: 0 });
  });

  it('createForTransport should post to transport endpoint', () => {
    service.createForTransport(3, { cargoDescription: 'Boxes', weightKg: 10, volumeM3: 1 }).subscribe();

    const req = httpMock.expectOne('/api/cargos/transport/3');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.cargoDescription).toBe('Boxes');
    req.flush({ id: 1 });
  });

  afterEach(() => {
    httpMock.verify();
  });
});







