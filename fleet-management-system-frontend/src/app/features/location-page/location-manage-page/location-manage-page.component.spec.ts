import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationManagePageComponent } from './location-manage-page.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('LocationManagePageComponent', () => {
  let component: LocationManagePageComponent;
  let fixture: ComponentFixture<LocationManagePageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LocationManagePageComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LocationManagePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



