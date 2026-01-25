import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssetsPageComponent } from './assets-page.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('AssetsPageComponent', () => {
  let component: AssetsPageComponent;
  let fixture: ComponentFixture<AssetsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AssetsPageComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AssetsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



