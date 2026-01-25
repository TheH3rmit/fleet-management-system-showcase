import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DriverWorkLogService } from './driver-work-log.service';

describe('DriverWorkLogService', () => {
  let service: DriverWorkLogService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(DriverWorkLogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});







