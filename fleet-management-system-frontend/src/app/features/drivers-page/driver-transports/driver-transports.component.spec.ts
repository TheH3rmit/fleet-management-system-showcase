import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { DriverTransportsComponent } from './driver-transports.component';

describe('DriverTransportsComponent', () => {
  let component: DriverTransportsComponent;
  let fixture: ComponentFixture<DriverTransportsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverTransportsComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DriverTransportsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



