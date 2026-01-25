import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminUserAccountDialogComponent } from './admin-user-account-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('AdminUserAccountDialogComponent', () => {
  let component: AdminUserAccountDialogComponent;
  let fixture: ComponentFixture<AdminUserAccountDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUserAccountDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        { provide: MAT_DIALOG_DATA, useValue: { mode: 'create', title: 'New user' } },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminUserAccountDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



