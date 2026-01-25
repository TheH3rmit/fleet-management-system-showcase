import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CargosManageComponent } from './cargos-manage.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('CargosManageComponent', () => {
  let component: CargosManageComponent;
  let fixture: ComponentFixture<CargosManageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CargosManageComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CargosManageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



