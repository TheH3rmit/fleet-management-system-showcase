import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TransportService } from './transport.service';
import { TransportStatus } from '../../models/transport-status.model';

describe('TransportService', () => {
  let service: TransportService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(TransportService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('list should build query params', () => {
    service.list({ status: TransportStatus.PLANNED, driverId: 5, q: 'wx', page: 1, size: 20 }).subscribe();

    const req = httpMock.expectOne((r: any) => r.url === '/api/transports');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('status')).toBe('PLANNED');
    expect(req.request.params.get('driverId')).toBe('5');
    expect(req.request.params.get('q')).toBe('wx');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('20');
    req.flush({ content: [], totalElements: 0 });
  });

  it('updateStatus should send PATCH body', () => {
    service.updateStatus(7, TransportStatus.ACCEPTED).subscribe();

    const req = httpMock.expectOne('/api/transports/7/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'ACCEPTED' });
    req.flush({ id: 7 });
  });

  afterEach(() => {
    httpMock.verify();
  });
});







