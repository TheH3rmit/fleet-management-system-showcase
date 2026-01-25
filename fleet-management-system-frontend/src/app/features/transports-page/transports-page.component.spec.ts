import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { TransportsPageComponent } from './transports-page.component';
import { TransportService } from '../../core/services/transport/transport.service';
import { NotificationService } from '../../core/services/notification/notification.service';
import { AuthService } from '../../core/services/auth/auth.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog.service';
import { DriverService } from '../../core/services/driver/driver.service';
import { VehicleService } from '../../core/services/vehicles/vehicle.service';
import { LocationService } from '../../core/services/location/location.service';
import { MatDialog } from '@angular/material/dialog';
import { TransportStatus } from '../../core/models/transport-status.model';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('TransportsPageComponent', () => {
  let component: TransportsPageComponent;
  let fixture: ComponentFixture<TransportsPageComponent>;
  let authService: AuthService;

  beforeEach(async () => {
    const transportServiceStub = {
      list: () => of({ content: [], totalElements: 0 }),
      delete: () => of(void 0),
    };
    const notifyStub = { success: () => {}, error: () => {} };
    const authStub = { hasRole: (_: string) => false };
    const confirmStub = { confirm: () => of(false) };
    const driverStub = { getManyByIds: () => of([]) };
    const vehicleStub = { getManyByIds: () => of([]) };
    const locationStub = { getAll: () => of([]) };
    const dialogStub = { open: () => ({ afterClosed: () => of(null) }) };

    await TestBed.configureTestingModule({
      imports: [TransportsPageComponent, NoopAnimationsModule, HttpClientTestingModule, MatSnackBarModule],
      providers: [
        { provide: TransportService, useValue: transportServiceStub },
        { provide: NotificationService, useValue: notifyStub },
        { provide: AuthService, useValue: authStub },
        { provide: ConfirmDialogService, useValue: confirmStub },
        { provide: DriverService, useValue: driverStub },
        { provide: VehicleService, useValue: vehicleStub },
        { provide: LocationService, useValue: locationStub },
        { provide: MatDialog, useValue: dialogStub },
      ],
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TransportsPageComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('parses status filter from search', () => {
    const parsed = (component as any).parseFilters('in progress');
    expect(parsed.status).toBe(TransportStatus.IN_PROGRESS);
    expect(parsed.driverId).toBeUndefined();
  });

  it('parses driver filter with prefix', () => {
    const parsed = (component as any).parseFilters('driver: 12');
    expect(parsed.driverId).toBe(12);
    expect(parsed.status).toBeUndefined();
  });

  it('driverLabel uses map when available', () => {
    (component as any).driverMap.set({
      7: { userId: 7, firstName: 'Jan', lastName: 'Kowalski' } as any
    });
    expect(component.driverLabel(7)).toBe('Jan Kowalski');
  });

  it('isAdmin reflects auth role', () => {
    spyOn(authService, 'hasRole').and.returnValue(true);
    expect(component.isAdmin()).toBeTrue();
  });
});



