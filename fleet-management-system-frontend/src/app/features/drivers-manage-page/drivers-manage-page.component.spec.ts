import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriversManagePageComponent } from './drivers-manage-page.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('DriversManagePageComponent', () => {
  let component: DriversManagePageComponent;
  let fixture: ComponentFixture<DriversManagePageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriversManagePageComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DriversManagePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



