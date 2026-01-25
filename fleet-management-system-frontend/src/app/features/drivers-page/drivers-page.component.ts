import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DriverTransportsComponent } from "./driver-transports/driver-transports.component";
import { MyCargoComponent } from "./my-cargo/my-cargo.component";
import { MyTimelineComponent } from "./my-timeline/my-timeline.component";
import { MatTabsModule } from "@angular/material/tabs";
import { MatProgressSpinner } from "@angular/material/progress-spinner";


@Component({
  selector: 'app-drivers-page',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    DriverTransportsComponent,
    MyCargoComponent,
    MyTimelineComponent,
  ],
  templateUrl: './drivers-page.component.html',
  styleUrl: './drivers-page.component.scss'
})
export class DriversPageComponent {

}



