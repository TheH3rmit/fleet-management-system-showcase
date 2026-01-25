import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyTimelineComponent } from './my-timeline.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('MyTimelineComponent', () => {
  let component: MyTimelineComponent;
  let fixture: ComponentFixture<MyTimelineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyTimelineComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MyTimelineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



