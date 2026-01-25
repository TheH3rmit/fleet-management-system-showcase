import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormControl, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatDialogRef, MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CreateTransportRequest, TransportDTO } from '../../../core/models/transport.model';
import { TransportService } from '../../../core/services/transport/transport.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { MatSelectModule } from "@angular/material/select";
import { VehicleService } from "../../../core/services/vehicles/vehicle.service";
import { TrailerService } from "../../../core/services/trailer/trailer.service";
import { LocationService } from '../../../core/services/location/location.service';
import { VehicleDTO } from "../../../core/models/vehicle.model";
import { TrailerDTO } from "../../../core/models/trailer.model";
import { LocationDTO } from "../../../core/models/location.model";
import { DriverService } from "../../../core/services/driver/driver.service";
import { DriverDTO } from "../../../core/models/driver.model";
import { catchError, forkJoin, of, switchMap, map, from } from "rxjs";
import { NotificationService } from '../../../core/services/notification/notification.service';
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { DriverStatus } from "../../../core/models/driver-status.model";
import { VehicleStatus } from "../../../core/models/vehicle-status.model";
import { TrailerStatus } from "../../../core/models/trailer-status.model";
import { LocationCreateDialogComponent } from "../../location-page/location-create-dialog/location-create-dialog.component";
import { MatIconModule } from "@angular/material/icon";
import { CargoService } from "../../../core/services/cargo/cargo.service";
import { CargoCreateDialogComponent } from "../../cargo-page/cargo-create-dialog/cargo-create-dialog.component";
import { MatCardModule } from "@angular/material/card";
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatTooltipModule } from '@angular/material/tooltip';

type CargoDraft = {
  cargoDescription: string;
  weightKg: number;
  volumeM3: number;
  pickupDate?: string | null;   // ISO string
  deliveryDate?: string | null; // ISO string
};

@Component({
  selector: 'app-transport-create-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule,
    MatCardModule,
    MatAutocompleteModule,
    MatTooltipModule
  ],
  templateUrl: './transport-create-dialog.component.html',
  styleUrl: './transport-create-dialog.component.scss'
})
export class TransportCreateDialogComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private ref = inject(MatDialogRef<TransportCreateDialogComponent>);
  private api = inject(TransportService);
  private auth = inject(AuthService);
  private notify = inject(NotificationService);
  private dialog = inject(MatDialog);

  private vehicleApi  = inject(VehicleService);
  private trailerApi  = inject(TrailerService);
  private locationApi = inject(LocationService);
  private driverApi = inject(DriverService);
  private cargoApi = inject(CargoService)
  vehicles: VehicleDTO[] = [];
  trailers: TrailerDTO[] = [];
  locations: LocationDTO[] = [];
  drivers: DriverDTO[] = [];

  loadingLists = false;
  creating = false;

  cargoDrafts = signal<CargoDraft[]>([]);

  vehicleSearchCtrl!: FormControl<string | number>;
  trailerSearchCtrl!: FormControl<string | number>;
  pickupSearchCtrl!: FormControl<string | number>;
  deliverySearchCtrl!: FormControl<string | number>;
  driverSearchCtrl!: FormControl<string | number>;

  vehicleQuery = signal('');
  trailerQuery = signal('');
  pickupQuery = signal('');
  deliveryQuery = signal('');
  driverQuery = signal('');

  form = this.fb.group({
    contractualDueAt: [''],              // '2025-01-01T10:00'
    plannedStartAt: ['', [Validators.required]],
    plannedEndAt: [''],
    plannedDistanceKm: [null as number | null, [Validators.min(0.01)]],
    trailerId: [null as number | null, [Validators.required]],
    vehicleId: [null as number | null, [Validators.required]],
    pickupLocationId: [null as number | null, [Validators.required]],
    deliveryLocationId: [null as number | null, [Validators.required]],
    driverId: [null as number | null],
  });

  ngOnInit(): void {
    this.initSearchControls();
    this.loadSelectData();
    this.bindSearchControls();
  }

  private initSearchControls() {
    this.vehicleSearchCtrl = new FormControl<string | number>('', {
      nonNullable: true,
      validators: [this.requiredSelectionValidator()],
    });
    this.trailerSearchCtrl = new FormControl<string | number>('', {
      nonNullable: true,
      validators: [this.requiredSelectionValidator()],
    });
    this.pickupSearchCtrl = new FormControl<string | number>('', {
      nonNullable: true,
      validators: [this.requiredSelectionValidator()],
    });
    this.deliverySearchCtrl = new FormControl<string | number>('', {
      nonNullable: true,
      validators: [this.requiredSelectionValidator()],
    });
    this.driverSearchCtrl = new FormControl<string | number>('', {
      nonNullable: true,
      validators: [this.optionalSelectionValidator()],
    });
  }

  private requiredSelectionValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      if (value == null || String(value).trim() === '') {
        return { required: true };
      }
      if (typeof value === 'number') {
        return null;
      }
      return { selection: true };
    };
  }

  private optionalSelectionValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      if (value == null || String(value).trim() === '') {
        return null;
      }
      if (typeof value === 'number') {
        return null;
      }
      return { selection: true };
    };
  }

  private bindSearchControls() {
    this.vehicleSearchCtrl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value: string | number) => {
        this.vehicleQuery.set(this.normalizeQuery(value, (v) => this.vehicleLabelById(v)));
        if (typeof value === 'string') this.form.controls.vehicleId.setValue(null);
        if (value === '') this.form.controls.vehicleId.setValue(null);
      });

    this.trailerSearchCtrl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value: string | number) => {
        this.trailerQuery.set(this.normalizeQuery(value, (v) => this.trailerLabelById(v)));
        if (typeof value === 'string') this.form.controls.trailerId.setValue(null);
        if (value === '') this.form.controls.trailerId.setValue(null);
      });

    this.pickupSearchCtrl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value: string | number) => {
        this.pickupQuery.set(this.normalizeQuery(value, (v) => this.locationLabelById(v)));
        if (typeof value === 'string') this.form.controls.pickupLocationId.setValue(null);
        if (value === '') this.form.controls.pickupLocationId.setValue(null);
      });

    this.deliverySearchCtrl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value: string | number) => {
        this.deliveryQuery.set(this.normalizeQuery(value, (v) => this.locationLabelById(v)));
        if (typeof value === 'string') this.form.controls.deliveryLocationId.setValue(null);
        if (value === '') this.form.controls.deliveryLocationId.setValue(null);
      });

    this.driverSearchCtrl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value: string | number) => {
        this.driverQuery.set(this.normalizeQuery(value, (v) => this.driverLabelById(v)));
        if (typeof value === 'string') this.form.controls.driverId.setValue(null);
        if (value === '') this.form.controls.driverId.setValue(null);
      });
  }

  private normalizeQuery(value: string | number, lookup: (id: number) => string): string {
    if (typeof value === 'number') return lookup(value).toLowerCase();
    return (value ?? '').toString().trim().toLowerCase();
  }

  private loadSelectData() {
    this.loadingLists = true;

    // Load only available assets/drivers for selection.
    forkJoin({
      vehicles: this.vehicleApi.getAvailable(),
      trailers: this.trailerApi.getAvailable(),
      locations: this.locationApi.getAll(),
      drivers: this.driverApi.getAvailable(),
    }).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ vehicles, trailers, locations, drivers }) => {
          this.vehicles = vehicles;
          this.trailers = trailers;
          this.locations = locations;
          this.drivers = [...drivers].sort((a, b) =>
            (a.lastName + a.firstName).localeCompare(b.lastName + b.firstName)
          );
        },
        error: () => {
          // Global httpErrorInterceptor will surface backend failures.
          this.notify.error('Nie udało się pobrać danych do formularza');
        },
        complete: () => {
          this.loadingLists = false;
        }
      });
  }
  driverLabel(d: DriverDTO) {
    return `${d.firstName} ${d.lastName} (userId: ${d.userId})`;
  }

  driverLabelById(id: number) {
    const d = this.drivers.find(x => x.userId === id);
    return d ? this.driverLabel(d) : `#${id}`;
  }

  vehicleLabelById(id: number) {
    const v = this.vehicles.find(x => x.id === id);
    if (!v) return `#${id}`;
    return `${v.licensePlate} - ${v.manufacturer} ${v.model} (id: ${v.id})`;
  }

  trailerLabelById(id: number) {
    const t = this.trailers.find(x => x.id === id);
    if (!t) return `#${id}`;
    return `${t.licensePlate} - ${t.name} (id: ${t.id})`;
  }

  locationLabel(loc: LocationDTO) {
    const parts = [
      loc.city,
      loc.street,
      loc.buildingNumber,
      loc.postcode,
    ].filter(Boolean);
    return `${parts.join(' ')} (id: ${loc.id})`.trim();
  }

  locationLabelById(id: number) {
    const loc = this.locations.find(x => x.id === id);
    return loc ? this.locationLabel(loc) : `#${id}`;
  }

  submit() {
    this.vehicleSearchCtrl.markAsTouched();
    this.trailerSearchCtrl.markAsTouched();
    this.pickupSearchCtrl.markAsTouched();
    this.deliverySearchCtrl.markAsTouched();
    this.driverSearchCtrl.markAsTouched();

    if (this.vehicleSearchCtrl.invalid ||
        this.trailerSearchCtrl.invalid ||
        this.pickupSearchCtrl.invalid ||
        this.deliverySearchCtrl.invalid ||
        this.driverSearchCtrl.invalid) {
      this.notify.warn('Select values from the lists before creating transport.');
      return;
    }

    if (this.form.invalid || this.creating) {
      this.form.markAllAsTouched();
      return;
    }

    this.creating = true;

    const v = this.form.value;
    const startAt = v.plannedStartAt ? new Date(v.plannedStartAt).getTime() : null;
    const endAt = v.plannedEndAt ? new Date(v.plannedEndAt).getTime() : null;
    if (startAt && endAt && endAt < startAt) {
      this.creating = false;
      this.notify.warn('Planned end cannot be before planned start.');
      return;
    }

    const toIso = (value?: string | null) => (value ? new Date(value).toISOString() : null);

    const dto: CreateTransportRequest = {
      contractualDueAt: toIso(v.contractualDueAt),
      plannedStartAt: toIso(v.plannedStartAt)!,
      plannedEndAt: toIso(v.plannedEndAt),
      plannedDistanceKm: v.plannedDistanceKm ?? null,
      trailerId: v.trailerId!,
      vehicleId: v.vehicleId!,
      pickupLocationId: v.pickupLocationId!,
      deliveryLocationId: v.deliveryLocationId!,
      driverId: v.driverId ?? null,
    };

    this.api.create(dto).pipe(
      switchMap((createdTransport: TransportDTO | any) => {
        const transportId: number | null =
          createdTransport?.id ?? createdTransport?.transportId ?? null;

        // If backend does not return an id, cargo drafts cannot be attached.
        if (!transportId) {
          // fallback: tworzymy tylko transport
          return from([{ ok: true, cargoErrors: 0 }]);
        }

        const drafts = this.cargoDrafts();

        if (!drafts.length) {
          return from([{ ok: true, cargoErrors: 0 }]);
        }

        const calls = drafts.map(d =>
          this.cargoApi.createForTransport(transportId, {
            cargoDescription: (d.cargoDescription ?? '').trim(),
            weightKg: d.weightKg,
            volumeM3: d.volumeM3,
            pickupDate: d.pickupDate ?? null,
            deliveryDate: d.deliveryDate ?? null,
          }).pipe(
            catchError(() => from([null]))
          )
        );

        return forkJoin(calls).pipe(
          map(results => {
            const cargoErrors = results.filter(x => x === null).length;
            return { ok: true, cargoErrors };
          })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ({ cargoErrors }) => {
        this.creating = false;

        if (cargoErrors > 0) {
          this.notify.warn(`Transport created, but ${cargoErrors} cargo item(s) failed to create.`);
        } else {
          this.notify.success('Transport created');
        }
        this.cargoDrafts.set([]);
        this.form.reset();

        this.ref.close('ok');
      },
      error: (err) => {
        this.creating = false;
        this.notify.error(err?.error?.message ?? 'Failed to create transport');
      }
    });
  }

  close() {
    this.ref.close();
  }

  availableDrivers(): DriverDTO[] {
    return (this.drivers ?? []).filter(d => d.driverStatus === DriverStatus.AVAILABLE);
  }

  availableVehicles(): VehicleDTO[] {
    return (this.vehicles ?? []).filter(v => v.vehicleStatus === VehicleStatus.ACTIVE);
  }

  availableTrailers(): TrailerDTO[] {
    return (this.trailers ?? []).filter(t => t.trailerStatus === TrailerStatus.ACTIVE);
  }

  filteredVehicles(): VehicleDTO[] {
    const q = this.vehicleQuery();
    const list = this.availableVehicles();
    if (!q) return list;
    return list.filter(v => this.vehicleLabelById(v.id).toLowerCase().includes(q));
  }

  filteredTrailers(): TrailerDTO[] {
    const q = this.trailerQuery();
    const list = this.availableTrailers();
    if (!q) return list;
    return list.filter(t => this.trailerLabelById(t.id).toLowerCase().includes(q));
  }

  filteredLocations(kind: 'pickup' | 'delivery'): LocationDTO[] {
    const q = kind === 'pickup' ? this.pickupQuery() : this.deliveryQuery();
    const list = this.locations ?? [];
    if (!q) return list;
    return list.filter(l => this.locationLabel(l).toLowerCase().includes(q));
  }

  filteredDrivers(): DriverDTO[] {
    const q = this.driverQuery();
    const list = this.availableDrivers();
    if (!q) return list;
    return list.filter(d => this.driverLabel(d).toLowerCase().includes(q));
  }

  onVehicleSelected(id: number | null) {
    if (id == null) {
      this.form.controls.vehicleId.setValue(null);
      this.vehicleSearchCtrl.setValue('');
      return;
    }
    this.form.controls.vehicleId.setValue(id);
  }

  onTrailerSelected(id: number | null) {
    if (id == null) {
      this.form.controls.trailerId.setValue(null);
      this.trailerSearchCtrl.setValue('');
      return;
    }
    this.form.controls.trailerId.setValue(id);
  }

  onPickupSelected(id: number | null) {
    if (id == null) {
      this.form.controls.pickupLocationId.setValue(null);
      this.pickupSearchCtrl.setValue('');
      return;
    }
    this.form.controls.pickupLocationId.setValue(id);
  }

  onDeliverySelected(id: number | null) {
    if (id == null) {
      this.form.controls.deliveryLocationId.setValue(null);
      this.deliverySearchCtrl.setValue('');
      return;
    }
    this.form.controls.deliveryLocationId.setValue(id);
  }

  onDriverSelected(id: number | null) {
    if (id == null) {
      this.form.controls.driverId.setValue(null);
      this.driverSearchCtrl.setValue('');
      return;
    }
    this.form.controls.driverId.setValue(id);
  }

  displayVehicle = (value: string | number | null) => {
    if (value == null) return '';
    return typeof value === 'number' ? this.vehicleLabelById(value) : value;
  };

  displayTrailer = (value: string | number | null) => {
    if (value == null) return '';
    return typeof value === 'number' ? this.trailerLabelById(value) : value;
  };

  displayPickup = (value: string | number | null) => {
    if (value == null) return '';
    return typeof value === 'number' ? this.locationLabelById(value) : value;
  };

  displayDelivery = (value: string | number | null) => {
    if (value == null) return '';
    return typeof value === 'number' ? this.locationLabelById(value) : value;
  };

  displayDriver = (value: string | number | null) => {
    if (value == null) return '';
    return typeof value === 'number' ? this.driverLabelById(value) : value;
  };

  openAddLocation(kind: 'pickup' | 'delivery') {
    const ref = this.dialog.open(LocationCreateDialogComponent, {
      width: '720px',
      panelClass: 'safe-dialog',
    });

    ref.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((created) => {
        if (!created) return;

        // Keep local list in sync and preselect the new location.
        this.locations = [created, ...this.locations];

        if (kind === 'pickup') {
          this.form.patchValue({ pickupLocationId: created.id });
          this.pickupSearchCtrl.setValue(created.id);
        }
        if (kind === 'delivery') {
          this.form.patchValue({ deliveryLocationId: created.id });
          this.deliverySearchCtrl.setValue(created.id);
        }
      });
  }




  openAddCargoDraft() {
    this.dialog.open(CargoCreateDialogComponent, {
      width: '520px',
      panelClass: 'safe-dialog',
      data: { transportId: null }
    }).afterClosed().pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((draft?: CargoDraft) => {
        if (!draft) return;
        this.cargoDrafts.set([...this.cargoDrafts(), draft]);
      });
  }

  openEditCargoDraft(index: number) {
    const current = this.cargoDrafts()[index];

    this.dialog.open(CargoCreateDialogComponent, {
      width: '520px',
      panelClass: 'safe-dialog',
      data: { transportId: null, draft: current }
    }).afterClosed().pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((draft?: CargoDraft) => {
        if (!draft) return;
        const next = [...this.cargoDrafts()];
        next[index] = draft;
        this.cargoDrafts.set(next);
      });
  }

  removeCargoDraft(index: number) {
    const next = [...this.cargoDrafts()];
    next.splice(index, 1);
    this.cargoDrafts.set(next);
  }


}



