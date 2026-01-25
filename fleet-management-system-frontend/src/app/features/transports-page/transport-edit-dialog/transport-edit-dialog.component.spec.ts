import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialog } from '@angular/material/dialog';
import { TransportEditDialogComponent } from './transport-edit-dialog.component';
import { TransportStatus } from '../../../core/models/transport-status.model';
import { DriverStatus } from '../../../core/models/driver-status.model';
import { VehicleStatus } from '../../../core/models/vehicle-status.model';
import { TrailerStatus } from '../../../core/models/trailer-status.model';
import { TransportService } from '../../../core/services/transport/transport.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { NotificationService } from '../../../core/services/notification/notification.service';
import { VehicleService } from '../../../core/services/vehicles/vehicle.service';
import { TrailerService } from '../../../core/services/trailer/trailer.service';
import { LocationService } from '../../../core/services/location/location.service';
import { DriverService } from '../../../core/services/driver/driver.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('TransportEditDialogComponent', () => {
  let component: TransportEditDialogComponent;
  let fixture: ComponentFixture<TransportEditDialogComponent>;

  beforeEach(async () => {
    const dialogRefStub = { close: () => {} };
    const transportServiceStub = { update: () => of({}) };
    const authStub = { hasRole: (_: string) => false };
    const notifyStub = { success: () => {}, error: () => {}, warn: () => {} };
    const vehicleStub = { getAll: () => of([]) };
    const trailerStub = { getAll: () => of([]) };
    const locationStub = { getAll: () => of([]) };
    const driverStub = { getAll: () => of([]) };
    const dialogStub = { open: () => ({}) };

    await TestBed.configureTestingModule({
      imports: [TransportEditDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: dialogRefStub },
        { provide: TransportService, useValue: transportServiceStub },
        { provide: AuthService, useValue: authStub },
        { provide: NotificationService, useValue: notifyStub },
        { provide: VehicleService, useValue: vehicleStub },
        { provide: TrailerService, useValue: trailerStub },
        { provide: LocationService, useValue: locationStub },
        { provide: DriverService, useValue: driverStub },
        { provide: MatDialog, useValue: dialogStub },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            transport: {
              id: 1,
              status: TransportStatus.PLANNED,
              plannedStartAt: null,
              plannedEndAt: null,
              plannedDistanceKm: null,
              pickupLocationId: null,
              deliveryLocationId: null,
              vehicleId: null,
              trailerId: null,
              driverId: null,
            },
          },
        },
      ],
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TransportEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('filters available drivers by status or current selection', () => {
    component.drivers = [
      { userId: 1, firstName: 'A', lastName: 'A', driverStatus: DriverStatus.AVAILABLE } as any,
      { userId: 2, firstName: 'B', lastName: 'B', driverStatus: DriverStatus.ON_TRANSPORT } as any,
    ];
    component.form.patchValue({ driverId: 2 });

    const list = component.availableDrivers();
    expect(list.map(d => d.userId)).toEqual([1, 2]);
  });

  it('filters vehicles by status or current selection', () => {
    component.vehicles = [
      { id: 1, manufacturer: 'X', model: 'Y', vehicleStatus: VehicleStatus.ACTIVE } as any,
      { id: 2, manufacturer: 'X', model: 'Z', vehicleStatus: VehicleStatus.IN_SERVICE } as any,
    ];
    component.form.patchValue({ vehicleId: 2 });

    const list = component.availableVehicles();
    expect(list.map(v => v.id)).toEqual([1, 2]);
  });

  it('filters trailers by status or current selection', () => {
    component.trailers = [
      { id: 1, name: 'T1', trailerStatus: TrailerStatus.ACTIVE } as any,
      { id: 2, name: 'T2', trailerStatus: TrailerStatus.IN_SERVICE } as any,
    ];
    component.form.patchValue({ trailerId: 2 });

    const list = component.availableTrailers();
    expect(list.map(t => t.id)).toEqual([1, 2]);
  });
});



