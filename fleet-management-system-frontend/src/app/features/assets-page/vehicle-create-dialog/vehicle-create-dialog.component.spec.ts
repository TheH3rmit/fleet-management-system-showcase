import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VehicleCreateDialogComponent } from './vehicle-create-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('VehicleCreateDialogComponent', () => {
  let component: VehicleCreateDialogComponent;
  let fixture: ComponentFixture<VehicleCreateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VehicleCreateDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [{ provide: MatDialogRef, useValue: { close: () => {} } }, { provide: MAT_DIALOG_DATA, useValue: {} }]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VehicleCreateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



