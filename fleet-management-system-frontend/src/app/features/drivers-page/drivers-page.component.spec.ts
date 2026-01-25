import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriversPageComponent } from './drivers-page.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('DriversPageComponent', () => {
  let component: DriversPageComponent;
  let fixture: ComponentFixture<DriversPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriversPageComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DriversPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



