import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrailerEditDialogComponent } from './trailer-edit-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('TrailerEditDialogComponent', () => {
  let component: TrailerEditDialogComponent;
  let fixture: ComponentFixture<TrailerEditDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrailerEditDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            trailer: {
              id: 1,
              name: 'Trailer 1',
              licensePlate: 'TR-001',
              trailerStatus: 'ACTIVE',
              payload: 12000,
              volume: 40,
              inProgressAssigned: false,
            },
          },
        },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TrailerEditDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



