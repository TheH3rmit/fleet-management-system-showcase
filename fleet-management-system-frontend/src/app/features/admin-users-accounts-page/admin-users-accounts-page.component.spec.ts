import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { AdminUsersAccountsPageComponent } from './admin-users-accounts-page.component';

describe('AdminUsersAccountsPageComponent', () => {
  let component: AdminUsersAccountsPageComponent;
  let fixture: ComponentFixture<AdminUsersAccountsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUsersAccountsPageComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule],
      providers: [provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminUsersAccountsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



