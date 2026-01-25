import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MyCargoComponent } from './my-cargo.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('MyCargoComponent', () => {
  let component: MyCargoComponent;
  let fixture: ComponentFixture<MyCargoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyCargoComponent, HttpClientTestingModule, NoopAnimationsModule, MatSnackBarModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MyCargoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});



