import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkLogManageComponent } from './work-log-manage.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('WorkLogManageComponent', () => {
  let component: WorkLogManageComponent;
  let fixture: ComponentFixture<WorkLogManageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkLogManageComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(WorkLogManageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



