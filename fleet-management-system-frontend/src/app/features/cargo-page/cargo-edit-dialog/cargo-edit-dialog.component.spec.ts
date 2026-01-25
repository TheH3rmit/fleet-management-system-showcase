import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CargoEditDialogComponent } from './cargo-edit-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('CargoEditDialogComponent', () => {
  let component: CargoEditDialogComponent;
  let fixture: ComponentFixture<CargoEditDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CargoEditDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            mode: 'edit',
            cargo: {
              id: 1,
              transportId: 101,
              cargoDescription: 'Boxes',
              weightKg: 100,
              volumeM3: 2,
              pickupDate: null,
              deliveryDate: null,
            },
          },
        },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CargoEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



