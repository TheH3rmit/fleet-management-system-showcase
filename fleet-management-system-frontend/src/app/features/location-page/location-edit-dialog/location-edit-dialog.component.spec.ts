import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationEditDialogComponent } from './location-edit-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('LocationEditDialogComponent', () => {
  let component: LocationEditDialogComponent;
  let fixture: ComponentFixture<LocationEditDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LocationEditDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            location: {
              id: 1,
              city: 'Warszawa',
              street: 'Testowa',
              buildingNumber: '1',
              postcode: '00-001',
              country: 'PL',
              latitude: null,
              longitude: null,
            },
          },
        },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LocationEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



