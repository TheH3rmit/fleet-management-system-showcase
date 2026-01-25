import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CargoCreateDialogComponent } from './cargo-create-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('CargoCreateDialogComponent', () => {
  let component: CargoCreateDialogComponent;
  let fixture: ComponentFixture<CargoCreateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CargoCreateDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        { provide: MAT_DIALOG_DATA, useValue: { transportId: null } },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CargoCreateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



