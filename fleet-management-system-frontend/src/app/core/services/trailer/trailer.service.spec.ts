import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TrailerService } from './trailer.service';

describe('TrailerService', () => {
  let service: TrailerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(TrailerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});







