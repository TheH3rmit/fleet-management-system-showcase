import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusHistoryDialogComponent } from './status-history-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('StatusHistoryDialogComponent', () => {
  let component: StatusHistoryDialogComponent;
  let fixture: ComponentFixture<StatusHistoryDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatusHistoryDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        { provide: MAT_DIALOG_DATA, useValue: { transportId: 1 } },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StatusHistoryDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



