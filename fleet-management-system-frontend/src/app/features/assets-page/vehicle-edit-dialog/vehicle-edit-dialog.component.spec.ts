import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VehicleEditDialogComponent } from './vehicle-edit-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('VehicleEditDialogComponent', () => {
  let component: VehicleEditDialogComponent;
  let fixture: ComponentFixture<VehicleEditDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VehicleEditDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            vehicle: {
              id: 1,
              manufacturer: 'Volvo',
              model: 'FH',
              licensePlate: 'WX-001',
              vehicleStatus: 'ACTIVE',
              dateOfProduction: null,
              mileage: null,
              fuelType: null,
              allowedLoad: null,
              insuranceNumber: null,
              inProgressAssigned: false,
            },
          },
        },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VehicleEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



