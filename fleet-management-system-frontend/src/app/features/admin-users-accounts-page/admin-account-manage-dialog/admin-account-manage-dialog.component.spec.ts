import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminAccountManageDialogComponent } from './admin-account-manage-dialog.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

describe('AdminAccountManageDialogComponent', () => {
  let component: AdminAccountManageDialogComponent;
  let fixture: ComponentFixture<AdminAccountManageDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminAccountManageDialogComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            account: {
              id: 1,
              login: 'admin',
              status: 'ACTIVE',
              roles: ['ADMIN'],
              createdAt: '2026-01-01T00:00:00Z',
              lastLoginAt: null,
            },
            user: { firstName: 'System', lastName: 'Admin', email: 'admin@local' },
          },
        },
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminAccountManageDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



