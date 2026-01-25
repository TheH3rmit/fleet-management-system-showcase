import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkLogFormDialogComponent } from './work-log-form-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('WorkLogFormDialogComponent', () => {
  let component: WorkLogFormDialogComponent;
  let fixture: ComponentFixture<WorkLogFormDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkLogFormDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        { provide: MAT_DIALOG_DATA, useValue: { mode: 'create' } },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(WorkLogFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



